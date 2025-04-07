package com.example.demo.service;

import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.model.CasoCorrigido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SugestaoCorrecaoService {


    @Autowired
    private SimilaridadeService similaridadeService;

    private CodeCorrectionService codeCorrectionService;


    public String sugerirCorrecao(ApontamentoDTO dto) {

        List<SimilaridadeService.CasoCorrigidoComSimilaridade> similares =
                similaridadeService.buscarSimilares(dto.getCodigo(), dto.getTipo());

        if (similares.isEmpty()) {
            return "Nenhum exemplo similar encontrado no banco.";
        }

        CasoCorrigido exemplo = similares.get(0).getCaso();

        if (similares.isEmpty()) {
            return "Nenhum exemplo similar encontrado no banco.";
        }

        // 3. Montar o payload com os campos adicionais
        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo_alvo", dto.getCodigo());
        payload.put("tipo", dto.getTipo());
        payload.put("linguagem", dto.getLinguagem());
        payload.put("contexto", dto.getContexto());

        List<Map<String, String>> exemplos = new ArrayList<>();
        Map<String, String> exemploMap = new HashMap<>();
        exemploMap.put("codigo_original", exemplo.getCodigoOriginal());
        exemploMap.put("codigo_corrigido", exemplo.getCodigoCorrigido());
        exemploMap.put("contexto", exemplo.getContexto());
        exemplos.add(exemploMap);

        payload.put("exemplos", exemplos);

        // 4. Enviar para o microserviço Python
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            String correcao = codeCorrectionService.gerarCorrecao(
                    dto.getTipo(), exemplo.getCodigoOriginal(), exemplo.getCodigoCorrigido(), dto.getCodigo()
            );

            if (correcao != null && !correcao.isEmpty()) {
                return correcao;
            } else {
                return "Nenhuma correção gerada.";
            }
        } catch (Exception e) {
            return "Erro ao realizar a correção: " + e.getMessage();
        }
    }

}
