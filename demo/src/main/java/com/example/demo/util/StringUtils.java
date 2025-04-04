package com.example.demo.util;

import com.example.demo.service.CodeCorrectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {

    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static String generatePrompt(String tipo, String codigoOriginal, String codigoCorrigido, String codigoAlvo) {
        return String.format("""
                Você é um assistente especializado em segurança de software. Dada uma vulnerabilidade do tipo %s,
                e um exemplo de código vulnerável com sua respectiva correção, você deve corrigir um novo código que contém a mesma falha.
                
                Exemplo:
                Código vulnerável:
                %s
                
                Código corrigido:
                %s
                
                Agora corrija o seguinte código:
                %s
                
                Código corrigido:
                """, tipo, codigoOriginal, codigoCorrigido, codigoAlvo);
    }

    public static String generatePrompt(String tipo, String codigoCorrigido, String codigoAlvo) {
        return String.format("""
                Você é um assistente especializado em fortify. Dada uma vulnerabilidade do tipo %s,
                e um exemplo de como corrigir a vulnerabilidade, você deve corrigir um novo código que contém a mesma falha aplicando uma abordagem semelhante.
                
                Código corrigido:
                %s
                
                Agora corrija o seguinte código:
                %s
                
                Código corrigido:
                """, tipo, codigoCorrigido, codigoAlvo);
    }

    public static String generatePromptWithOutBase(String tipo, String codigoAlvo) {
        return String.format("""
                Você é um assistente especializado em fortify. Dada uma vulnerabilidade do tipo %s,
                corrija-a da maneira menos invasiva possível, retorne apenas o código corrigido.
               
                Cídigo a ser corrigido:
                %s
                
                Código corrigido:
                """, tipo, codigoAlvo);
    }
}
