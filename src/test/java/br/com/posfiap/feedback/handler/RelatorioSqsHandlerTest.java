package br.com.posfiap.feedback.handler;

import br.com.posfiap.feedback.service.RelatorioSemanalService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class RelatorioSqsHandlerTest {

    @Mock
    RelatorioSemanalService service;

    @Mock
    Context context;

    @InjectMocks
    RelatorioSqsHandler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveProcessarEventoSqsComSucesso() {
        // given
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("{\"tipo\":\"RELATORIO_SEMANAL\"}");
        event.setRecords(List.of(message));

        // when
        handler.handleRequest(event, context);

        // then
        verify(service, times(1)).gerarRelatorioSemanal();
    }

    @Test
    void naoDeveGerarRelatorioQuandoNaoHaMensagens() {
        SQSEvent event = new SQSEvent();
        event.setRecords(List.of());

        handler.handleRequest(event, context);

        verify(service, never()).gerarRelatorioSemanal();
    }

}
