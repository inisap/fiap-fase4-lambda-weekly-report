package br.com.posfiap.feedback.service;

import br.com.posfiap.feedback.model.AvaliacaoEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RelatorioSemanalService {

    private static final Logger LOG = Logger.getLogger(RelatorioSemanalService.class);

    @Inject
    DynamoDbEnhancedClient dynamo;

    @Inject
    S3Client s3;

    /**
     * Configurações injetáveis (produção via env, teste via setter)
     */
    private String dynamoTable;
    private String relatorioBucket;

    void setDynamoTable(String dynamoTable) {
        this.dynamoTable = dynamoTable;
    }

    void setRelatorioBucket(String relatorioBucket) {
        this.relatorioBucket = relatorioBucket;
    }

    public void gerarRelatorioSemanal() {

        String tableName = dynamoTable != null
                ? dynamoTable
                : System.getenv("DYNAMODB_TABLE");

        String bucket = relatorioBucket != null
                ? relatorioBucket
                : System.getenv("RELATORIO_BUCKET");

        if (tableName == null || bucket == null) {
            throw new IllegalStateException("Variáveis de ambiente DYNAMODB_TABLE ou RELATORIO_BUCKET não configuradas");
        }

        DynamoDbTable<AvaliacaoEntity> table =
                dynamo.table(tableName, TableSchema.fromBean(AvaliacaoEntity.class));

        List<AvaliacaoEntity> avaliacoes = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            LocalDate dia = LocalDate.now(ZoneOffset.UTC).minusDays(i);
            String pk = "DATE#" + dia;

            LOG.infof("Consultando DynamoDB PK=%s", pk);

            QueryConditional query =
                    QueryConditional.keyEqualTo(Key.builder().partitionValue(pk).build());

            table.query(query).items().forEach(avaliacoes::add);
        }

        LOG.infof("Total de avaliações coletadas: %d", avaliacoes.size());

        Map<LocalDate, Long> qtdPorDia =
                avaliacoes.stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getDataCriacao()
                                        .atZone(ZoneId.of("America/Sao_Paulo"))
                                        .toLocalDate(),
                                Collectors.counting()
                        ));

        Map<String, Long> qtdPorUrgencia =
                avaliacoes.stream()
                        .collect(Collectors.groupingBy(
                                a -> urgencia(a.getNota()),
                                Collectors.counting()
                        ));

        Path csv = gerarCsv(avaliacoes, qtdPorDia, qtdPorUrgencia);

        String key = "relatorio-semanal-" + LocalDate.now() + ".csv";

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/csv")
                        .build(),
                csv
        );

        LOG.infof("CSV enviado para s3://%s/%s", bucket, key);
    }

    private Path gerarCsv(
            List<AvaliacaoEntity> dados,
            Map<LocalDate, Long> porDia,
            Map<String, Long> porUrgencia
    ) {
        try {
            Path file = Files.createTempFile("relatorio-semanal", ".csv");

            StringBuilder sb = new StringBuilder();
            sb.append("descricao,urgencia,data_envio\n");

            for (AvaliacaoEntity a : dados) {
                sb.append(a.getDescricao()).append(",")
                        .append(urgencia(a.getNota())).append(",")
                        .append(a.getDataCriacao()).append("\n");
            }

            sb.append("\nQuantidade por dia\n");
            porDia.forEach((dia, qtd) ->
                    sb.append(dia).append(",").append(qtd).append("\n")
            );

            sb.append("\nQuantidade por urgencia\n");
            porUrgencia.forEach((urg, qtd) ->
                    sb.append(urg).append(",").append(qtd).append("\n")
            );

            Files.writeString(file, sb.toString());
            return file;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }
    }

    private String urgencia(int nota) {
        if (nota <= 4) return "ALTA";
        if (nota <= 7) return "MEDIA";
        return "BAIXA";
    }
}
