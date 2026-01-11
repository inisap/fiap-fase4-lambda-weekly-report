package br.com.posfiap.feedback.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoEntityTest {

    @Test
    void deveCriarAvaliacaoComPkSkCorretos() {
        // given
        String descricao = "Curso muito bom";
        int nota = 5;

        LocalDate hojeUtc = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        // when
        AvaliacaoEntity entity = AvaliacaoEntity.from(descricao, nota);

        // then
        assertNotNull(entity);

        // PK
        assertEquals("DATE#" + hojeUtc, entity.getPk());

        // SK
        assertNotNull(entity.getSk());
        assertTrue(entity.getSk().startsWith("NOTA#" + nota + "#"));

        // valida UUID na SK
        String[] skParts = entity.getSk().split("#");
        assertEquals(3, skParts.length);
        assertDoesNotThrow(() -> java.util.UUID.fromString(skParts[2]));

        // campos de negócio
        assertEquals(descricao, entity.getDescricao());
        assertEquals(nota, entity.getNota());

        // data de criação
        assertNotNull(entity.getDataCriacao());
        assertTrue(entity.getDataCriacao().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void deveCriarSkComNotaCorreta() {
        AvaliacaoEntity e = AvaliacaoEntity.from("Teste", 10);

        assertTrue(e.getSk().startsWith("NOTA#10#"));
    }

}
