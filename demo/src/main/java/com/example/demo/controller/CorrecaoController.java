package com.example.demo.controller;

import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.dto.SugestaoCorrecaoDTO;
import com.example.demo.model.CasoCorrigido;
import com.example.demo.service.CodeCorrectionService;
import com.example.demo.service.SimilaridadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sugerir-correcao")
public class CorrecaoController {


    @Autowired
    private CodeCorrectionService codeCorrectionService;

    @Autowired
    private SimilaridadeService similaridadeService;

    @PostMapping("/corrigir")
    public SugestaoCorrecaoDTO corrigir(@RequestBody ApontamentoDTO dto) {
        // Agora passamos apenas o código e tipo diretamente para o serviço
        List<SimilaridadeService.CasoCorrigidoComSimilaridade> similares =
                similaridadeService.buscarSimilares(dto.getCodigo(), dto.getTipo());

        if (similares.isEmpty()) {
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

        SimilaridadeService.CasoCorrigidoComSimilaridade melhor = similares.get(0);
        CasoCorrigido c = melhor.getCaso();

        String correcaoGerada = codeCorrectionService.gerarCorrecao(
                dto.getTipo(),
                dto.getCodigo(),
                c.getCodigoOriginal(),
                c.getCodigoCorrigido()
        );

        return new SugestaoCorrecaoDTO(
                dto.getTipo(),
                dto.getCodigo(),
                correcaoGerada,
                melhor.getSimilaridade()
        );
    }


    @PostMapping("/cadastrar-caso")
    public ResponseEntity<Map<String, String>> cadastrarCaso(@RequestBody CasoCorrigido novoCaso) {
        Map<String, String> resposta = new HashMap<>();

        // Validação do campo 'tipo'
        if (novoCaso.getTipo() == null || novoCaso.getTipo().trim().isEmpty()) {
            resposta.put("mensagem", "O campo 'tipo' é obrigatório.");
            return ResponseEntity.badRequest().body(resposta);
        }

        // Normalização do tipo (opcional, pode ajustar conforme o padrão que quiser)
        String tipoNormalizado = novoCaso.getTipo().trim();
        tipoNormalizado = tipoNormalizado.substring(0, 1).toUpperCase() + tipoNormalizado.substring(1).toLowerCase();
        novoCaso.setTipo(tipoNormalizado);

        boolean sucesso = similaridadeService.inserirCasoCorrigido(novoCaso);

        if (sucesso) {
            resposta.put("mensagem", "Caso inserido com sucesso.");
            return ResponseEntity.ok(resposta);
        } else {
            resposta.put("mensagem", "Erro ao inserir caso.");
            return ResponseEntity.status(500).body(resposta);
        }
    }

}