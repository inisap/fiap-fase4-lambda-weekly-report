package br.com.posfiap.feedback.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

@DynamoDbBean
public class AvaliacaoEntity {

    private String pk;
    private String sk;
    private String descricao;
    private int nota;
    private Instant dataCriacao;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Instant dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    /**
     * Cria uma avaliação com PK/SK modeladas para consultas por dia e nota
     */
    public static AvaliacaoEntity from(String descricao, int nota) {

        AvaliacaoEntity e = new AvaliacaoEntity();

        LocalDate hojeUtc = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        e.setPk("DATE#" + hojeUtc);
        e.setSk("NOTA#" + nota + "#" + UUID.randomUUID());

        e.setDescricao(descricao);
        e.setNota(nota);
        e.setDataCriacao(Instant.now());

        return e;
    }
}
