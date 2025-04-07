package com.example.demo.controller;

import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.dto.CodigoCorrigidoComSimilaridadeDTO;
import com.example.demo.dto.SugestaoCorrecaoDTO;
import com.example.demo.model.CasoCorrigido;
import com.example.demo.service.CodeCorrectionService;
import com.example.demo.service.SimilaridadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

;

@RestController
@RequestMapping("/sugerir-correcao")
public class CorrecaoController {

    private static final Logger logger = LoggerFactory.getLogger(CorrecaoController.class);

    @Autowired
    private CodeCorrectionService codeCorrectionService;

    @Autowired
    private SimilaridadeService similaridadeService;

    @PostMapping("/corrigir")
    public SugestaoCorrecaoDTO corrigir(@RequestBody ApontamentoDTO dto) {
        logger.info("Recebida solicitação de correção. Tipo: {}, Código:\n{}", dto.getTipo(), dto.getCodigo());

        Optional<CodigoCorrigidoComSimilaridadeDTO> exemploCorrigidoOptional =
                similaridadeService.obterCodigoCorrigidoMaisSimilar(dto.getCodigo(), dto.getTipo());

        if (exemploCorrigidoOptional.isEmpty()) {
            logger.warn("Nenhum exemplo similar encontrado para o código fornecido.");
            return new SugestaoCorrecaoDTO(
                    dto.getTipo(),
                    dto.getLinguagem(),
                    dto.getContexto(),
                    dto.getCodigo(),
                    "Nenhuma sugestão encontrada",
                    "Não foram encontrados casos semelhantes no banco de dados.",
                    0.0
            );
        }

        CodigoCorrigidoComSimilaridadeDTO resultado = exemploCorrigidoOptional.get();
        String exemploCorrigido = resultado.getCodigoCorrigido();
        double similaridade = resultado.getSimilaridade();

        logger.info("Código corrigido mais semelhante recuperado:\n{}", exemploCorrigido);

        String correcaoGerada = codeCorrectionService.gerarCorrecao(
                dto.getTipo(),
                dto.getCodigo(),
                exemploCorrigido,
                exemploCorrigido
        );

        logger.info("Correção gerada pela IA:\n{}", correcaoGerada);

        double similaridadeFormatada = new BigDecimal(similaridade)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();

        return new SugestaoCorrecaoDTO(
                dto.getTipo(),
                dto.getCodigo(),
                correcaoGerada,
                similaridadeFormatada
        );

    }

    @PostMapping("/cadastrar-caso")
    public ResponseEntity<Map<String, String>> cadastrarCaso(@RequestBody CasoCorrigido novoCaso) {
        Map<String, String> resposta = new HashMap<>();

        if (novoCaso.getTipo() == null || novoCaso.getTipo().trim().isEmpty()) {
            resposta.put("mensagem", "O campo 'tipo' é obrigatório.");
            return ResponseEntity.badRequest().body(resposta);
        }

        String tipoNormalizado = novoCaso.getTipo().trim();
        tipoNormalizado = tipoNormalizado.substring(0, 1).toUpperCase() + tipoNormalizado.substring(1).toLowerCase();
        novoCaso.setTipo(tipoNormalizado);

        boolean sucesso = similaridadeService.inserirCasoCorrigido(novoCaso);

        if (sucesso) {
            logger.info("Novo caso corrigido cadastrado com sucesso:\n{}", novoCaso.getCodigoCorrigido());
            resposta.put("mensagem", "Caso inserido com sucesso.");
            return ResponseEntity.ok(resposta);
        } else {
            logger.error("Erro ao tentar cadastrar o caso.");
            resposta.put("mensagem", "Erro ao inserir caso.");
            return ResponseEntity.status(500).body(resposta);
        }
    }
}
