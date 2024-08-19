package br.ufscar.dc.compiladores;

import java.util.LinkedList;

import br.ufscar.dc.compiladores.TabelaDeSimbolos.TipoDeclaracao;

public class Escopo {
    private LinkedList<TabelaDeSimbolos> pilhaDeTabelas;

    public Escopo(){
        pilhaDeTabelas = new LinkedList<TabelaDeSimbolos>();
        criarNovoEscopo();
    }

    public void criarNovoEscopo(){
        pilhaDeTabelas.push(new TabelaDeSimbolos());
    }

    public TabelaDeSimbolos removerEscopo(){
        return pilhaDeTabelas.pop();
    }

    public TabelaDeSimbolos escopoAtual(){
        return pilhaDeTabelas.peek();
    }

    public LinkedList<TabelaDeSimbolos> recuperarTodosEscopos(){
        return pilhaDeTabelas;
    }

    // Verifica a existência de uma variável em todas as tabelas do escopo
    public boolean existe(String nomeVar){
        for (TabelaDeSimbolos tabela: recuperarTodosEscopos()){
            if (tabela.existe(nomeVar)){
                return true;
            }
        }
        return false;
    }

    // Obtém o tipo de uma variável existente no escopo
    public TipoDeclaracao verificaTodosEscopos(String nomeVar){
        for (TabelaDeSimbolos tabela: recuperarTodosEscopos()){
            if (tabela.existe(nomeVar)){
                return tabela.verificar(nomeVar);
            }
        }
        
        return TipoDeclaracao.INVALIDO;
    }
}
