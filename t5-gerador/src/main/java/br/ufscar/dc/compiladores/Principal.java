package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.LAParser.ProgramaContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CommonTokenStream;

public class Principal 
{
    public static void main(String args[]) throws IOException {
        PrintWriter fwriter = null;

        // Altera a saída do programa de acordo com a quantidade de argumentos
        switch (args.length) {
            case 1:
                // Opção para entrada de um único argumento de entrada e saída na saída padrão
                fwriter = new PrintWriter(System.out);
                fwriter.println();
                break;
            case 2:
                // Opção padrão para dois argumentos, saída em arquivo
                try {
                    fwriter = new PrintWriter(new File(args[1]));
                } catch (Exception e) {
                    System.out.println("Falha ao abrir o arquivo");
                    e.printStackTrace();
                } 
                break;
            default:
                // Número inválido de argumentos
                System.out.println("Número inválido de argumentos!");
                System.out.println("Recebeu " + args.length + " argumentos, esperava no mínimo 1:");
                System.out.println("<caminho para o código fonte LA> [caminho para arquivo de saída]");
                return; // Termina o programa prematuramente
        }

        final PrintWriter writer = fwriter;

        // Análise Léxica
        // --------------
        
        CharStream cs = CharStreams.fromFileName(args[0]);
        LALexer lexer = new LALexer(cs);
        Boolean erroLexico = false;

        Token t = null;
        while ((t = lexer.nextToken()).getType() != Token.EOF) {
            String nomeToken = LALexer.VOCABULARY.getDisplayName(t.getType());
            
            // Mensagem de erro para qualquer simbolo não identificado. 
            if(nomeToken.equals("ERRO")) {
                writer.println("Linha "+t.getLine()+": "+t.getText()+" - simbolo nao identificado");
                erroLexico = true;
                break;
            }
            // Mensagem de erro customizada para comentários não fechados.
            else if(nomeToken.equals("COMENT_N_FECHADO")) {
                writer.println("Linha "+t.getLine()+": comentario nao fechado");
                erroLexico = true;
                break;
            }
            
            // Mensagem de erro customizada para strings não fechadas.
            else if(nomeToken.equals("CADEIA_N_FECHADA")) {
                writer.println("Linha "+t.getLine()+": cadeia literal nao fechada");
                erroLexico = true;
                break;
            }
        }

        // Análise Sintática
        // -----------------

        if (!erroLexico) {
            cs = CharStreams.fromFileName(args[0]);
            lexer = new LALexer(cs);
    
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LAParser parser = new LAParser(tokens);
    
            // Adicionando nosso ErrorListener customizado
            parser.removeErrorListeners();
            MyCustomErrorListener mcel = new MyCustomErrorListener(writer);
            parser.addErrorListener(mcel);
    
            parser.programa();
        }

        // Análise Semântica
        // -----------------

        cs = CharStreams.fromFileName(args[0]);
        lexer = new LALexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LAParser parser = new LAParser(tokens);

        parser.removeErrorListeners();
        
        ProgramaContext arvore = parser.programa();
        LASemantico as = new LASemantico();

        as.visitPrograma(arvore);
        LASemanticoUtils.errosSemanticos.forEach((s) -> writer.println(s));
        
        // Geração de Código C
        // -------------------

        if(LASemanticoUtils.errosSemanticos.isEmpty()) {
            LAGeradorC agc = new LAGeradorC();
            agc.visitPrograma(arvore);
            writer.print(agc.saida.toString());
        }

        // writer.println("Fim da compilacao");
        writer.close();
    }
}
