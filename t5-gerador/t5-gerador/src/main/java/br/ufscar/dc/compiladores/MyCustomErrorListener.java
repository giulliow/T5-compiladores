package br.ufscar.dc.compiladores;

import java.io.PrintWriter;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class MyCustomErrorListener implements ANTLRErrorListener {
    PrintWriter pw;
    Boolean erroSintatico = false;
    public MyCustomErrorListener(PrintWriter pw) {
        this.pw = pw;    
    }

    @Override
    public void	reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        // Não será necessário para o T2, pode deixar vazio
    }
    
    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        // Não será necessário para o T2, pode deixar vazio
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        // Não será necessário para o T2, pode deixar vazio
    }

    @Override
    public void	syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Reportar apenas o primeiro erro, ignorar os outros
        if(!erroSintatico){
            Token t = (Token) offendingSymbol;

            // Modificação da string "EOF" para conformidade com os casos de testes
            String tokenText = t.getText();
            if (tokenText == "<EOF>"){
                tokenText = "EOF";
            }
    
            // Impressão do erro
            pw.println("Linha "+line+": erro sintatico proximo a "+ tokenText);
            erroSintatico = true;
        }
    }
}
