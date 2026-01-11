package br.com.posfiap.feedback.service;

import br.com.posfiap.feedback.model.AvaliacaoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelatorioSemanalServiceTest {

    @Mock
    DynamoDbEnhancedClient dynamo;

    @Mock
    DynamoDbTable<AvaliacaoEntity> table;

    @Mock
    S3Client s3;

    @InjectMocks
    RelatorioSemanalService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // configurações injetáveis (sem System.getenv)
        service.setDynamoTable("avaliacoes");
        service.setRelatorioBucket("bucket-relatorios");
    }

    @Test
    void deveGerarRelatorioSemanalEEnviarParaS3() {

        // given
        AvaliacaoEntity a1 = AvaliacaoEntity.from("Curso A", 3);
        AvaliacaoEntity a2 = AvaliacaoEntity.from("Curso B", 8);

        a1.setDataCriacao(Instant.now().minusSeconds(3600));
        a2.setDataCriacao(Instant.now().minusSeconds(7200));

        Page<AvaliacaoEntity> page =
                Page.create(List.of(a1, a2));

        SdkIterable<Page<AvaliacaoEntity>> sdkIterable =
                () -> List.of(page).iterator();

        PageIterable<AvaliacaoEntity> pageIterable =
                PageIterable.create(sdkIterable);

        when(dynamo.table(eq("avaliacoes"), any(TableSchema.class)))
                .thenReturn(table);

        when(table.query(any(QueryConditional.class)))
                .thenReturn(pageIterable);

        // when
        service.gerarRelatorioSemanal();

        // then
        verify(dynamo, times(1))
                .table(eq("avaliacoes"), any(TableSchema.class));

        verify(table, atLeastOnce())
                .query(any(QueryConditional.class));

        verify(s3, times(1))
                .putObject(
                        argThat((PutObjectRequest req) ->
                                "bucket-relatorios".equals(req.bucket())
                                        && req.key().startsWith("relatorio-semanal-")
                                        && "text/csv".equals(req.contentType())
                        ),
                        any(Path.class)
                );
    }

    @Test
    void deveGerarRelatorioMesmoSemAvaliacoes() {

        service.setDynamoTable("avaliacoes");
        service.setRelatorioBucket("bucket-relatorios");

        Page<AvaliacaoEntity> emptyPage =
                Page.create(List.of());

        PageIterable<AvaliacaoEntity> pageIterable =
                PageIterable.create(() -> List.of(emptyPage).iterator());

        when(dynamo.table(eq("avaliacoes"), any(TableSchema.class)))
                .thenReturn(table);

        when(table.query(any(QueryConditional.class)))
                .thenReturn(pageIterable);

        service.gerarRelatorioSemanal();

        verify(s3, times(1))
                .putObject(any(PutObjectRequest.class), any(Path.class));
    }

    @Test
    void deveClassificarUrgenciaCorretamente() {

        assertEquals("ALTA", invokeUrgencia(3));
        assertEquals("MEDIA", invokeUrgencia(6));
        assertEquals("BAIXA", invokeUrgencia(9));
    }

    /**
     * Usa reflexão controlada apenas para aumentar coverage
     */
    private String invokeUrgencia(int nota) {
        try {
            var m = RelatorioSemanalService.class
                    .getDeclaredMethod("urgencia", int.class);
            m.setAccessible(true);
            return (String) m.invoke(service, nota);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deveAgruparAvaliacoesPorDiaEUrgencia() {

        service.setDynamoTable("avaliacoes");
        service.setRelatorioBucket("bucket-relatorios");

        AvaliacaoEntity a1 = AvaliacaoEntity.from("A", 2); // ALTA
        AvaliacaoEntity a2 = AvaliacaoEntity.from("B", 6); // MEDIA
        AvaliacaoEntity a3 = AvaliacaoEntity.from("C", 9); // BAIXA

        Instant now = Instant.now();
        a1.setDataCriacao(now.minusSeconds(3600));
        a2.setDataCriacao(now.minusSeconds(3600));
        a3.setDataCriacao(now.minusSeconds(7200));

        Page<AvaliacaoEntity> page =
                Page.create(List.of(a1, a2, a3));

        PageIterable<AvaliacaoEntity> pageIterable =
                PageIterable.create(() -> List.of(page).iterator());

        when(dynamo.table(eq("avaliacoes"), any(TableSchema.class)))
                .thenReturn(table);

        when(table.query(any(QueryConditional.class)))
                .thenReturn(pageIterable);

        service.gerarRelatorioSemanal();

        verify(s3, times(1))
                .putObject(any(PutObjectRequest.class), any(Path.class));
    }



}
