package br.com.posfiap.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import br.com.posfiap.feedback.service.RelatorioSemanalService;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

public class RelatorioSqsHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger LOG = Logger.getLogger(RelatorioSqsHandler.class);

    @Inject
    RelatorioSemanalService service;

    @Override
    public Void handleRequest(SQSEvent event, Context context) {

        LOG.info("Lambda de relatório semanal acionada");

        if (event == null || event.getRecords() == null || event.getRecords().isEmpty()) {
            LOG.warn("Evento SQS sem mensagens");
            return null;
        }

        event.getRecords().forEach(record -> {
            LOG.infof("Mensagem recebida da SQS: %s", record.getBody());
        });

        service.gerarRelatorioSemanal();

        LOG.info("Relatório semanal finalizado com sucesso");
        return null;
    }
}
