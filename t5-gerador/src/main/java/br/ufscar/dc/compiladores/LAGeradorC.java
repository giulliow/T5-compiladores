package br.ufscar.dc.compiladores;

import java.util.Iterator;

import br.ufscar.dc.compiladores.LAParser.CmdChamadaContext;
import br.ufscar.dc.compiladores.LAParser.CmdContext;
import br.ufscar.dc.compiladores.LAParser.CmdEnquantoContext;
import br.ufscar.dc.compiladores.LAParser.CmdFacaContext;
import br.ufscar.dc.compiladores.LAParser.CmdParaContext;
import br.ufscar.dc.compiladores.LAParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.LAParser.Declaracao_funcoesContext;
import br.ufscar.dc.compiladores.LAParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.LAParser.ExpressaoContext;
import br.ufscar.dc.compiladores.LAParser.FatorContext;
import br.ufscar.dc.compiladores.LAParser.IdentificadorContext;
import br.ufscar.dc.compiladores.LAParser.Numero_intervaloContext;
import br.ufscar.dc.compiladores.LAParser.ParcelaContext;
import br.ufscar.dc.compiladores.LAParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.LAParser.TermoContext;
import br.ufscar.dc.compiladores.TabelaDeSimbolos.TipoDeclaracao;

public class LAGeradorC extends LABaseVisitor<Void>{
    StringBuilder saida;
    Escopo escopo;

    public LAGeradorC() {
        saida = new StringBuilder();
        this.escopo = new Escopo();
    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        // Declaração inicial de bibliotecas
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stdlib.h>\n");
        saida.append("\n");
        escopo.criarNovoEscopo();

        // Área de declaração de variáveis globais e funções
        ctx.declaracoes().declaracao_variaveis()
            .forEach(dec -> visitDeclaracao_variaveis(dec));
        ctx.declaracoes().declaracao_funcoes()
            .forEach(dec -> visitDeclaracao_funcoes(dec));

        // Início da função principal
        saida.append("\nint main() {\n");
        escopo.criarNovoEscopo();

        ctx.corpo().declaracao_variaveis()
            .forEach(dec -> visitDeclaracao_variaveis(dec));
        ctx.corpo().cmd()
            .forEach(cmd -> visitCmd(cmd));
        saida.append("\treturn 0;\n}\n");
        return null;
    }

    @Override
    public Void visitDeclaracao_funcoes(Declaracao_funcoesContext ctx) {
        TipoDeclaracao tipoRetorno = TipoDeclaracao.PROCEDIMENTO;

        if (ctx.FUNCAO() != null){
            tipoRetorno = LASemanticoUtils.verificarTipo(escopo, ctx.tipo_variavel());
            String strCTipo = LAGeradorUtils.mapTipoC(tipoRetorno);

            saida.append(strCTipo + " ");
        }
        else{
            saida.append("void ");
        }

        saida.append(ctx.IDENT().getText() + " (");

        if (ctx.parametros() != null){
            TipoDeclaracao tipoParametro = LASemanticoUtils.verificarTipo(escopo, ctx.parametros().parametro(0).tipo_variavel());
            String strCTipoParametro = LAGeradorUtils.mapTipoC(tipoParametro);
            TabelaDeSimbolos parametros = new TabelaDeSimbolos();

            parametros.adicionar(ctx.parametros().parametro(0).identificador(0).getText(), tipoParametro);
            escopo.escopoAtual().adicionar(ctx.IDENT().getText(), tipoRetorno, parametros);
            saida.append(strCTipoParametro);

            if (tipoParametro == TipoDeclaracao.LITERAL){
                saida.append("*");
            }
            
            saida.append(" " + ctx.parametros().parametro(0).identificador(0).getText());
        }

        saida.append("){\n");
        escopo.criarNovoEscopo();

        if (ctx.parametros() != null){
            TipoDeclaracao tipoParametro = LASemanticoUtils.verificarTipo(escopo, ctx.parametros().parametro(0).tipo_variavel());
            escopo.escopoAtual().adicionar(ctx.parametros().parametro(0).identificador(0).getText(), tipoParametro);
        }

        ctx.declaracao_variaveis().forEach(dec -> visitDeclaracao_variaveis(dec));
        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        saida.append("}\n");

        return null;
    }

    @Override
    public Void visitDeclaracao_variaveis(LAParser.Declaracao_variaveisContext ctx){
        
        if(ctx.DECLARE() != null){
            visitVariavel(ctx.variavel());
        }
        else if (ctx.CONSTANTE() != null){
            saida.append("#define " + ctx.IDENT() + " " + ctx.valor_constante().getText() + "\n");
        }
        else if (ctx.TIPO() != null){
            escopo.criarNovoEscopo();
            saida.append("\ttypedef struct{\n");
            visitRegistro(ctx.registro());
            saida.append("\t} " + ctx.IDENT().getText() + ";\n");

            TabelaDeSimbolos dadosTipo = escopo.escopoAtual();

            escopo.escopoAtual().adicionarTipo(ctx.IDENT().getText(), dadosTipo);
        }

        return null;
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx){
        TipoDeclaracao tipoVariavel = LASemanticoUtils.verificarTipo(escopo, ctx.tipo());

        if (tipoVariavel != TipoDeclaracao.REGISTRO && tipoVariavel != TipoDeclaracao.TIPO){
            String strCtipo = LAGeradorUtils.mapTipoC(tipoVariavel);

            if (tipoVariavel == TipoDeclaracao.PONTEIRO){
                TipoDeclaracao tipoSemPonteiro = LAGeradorUtils.mapTipoDeclaracao(ctx.tipo().getText().replace("^", ""));
                strCtipo = LAGeradorUtils.mapTipoC(tipoSemPonteiro);
            }

            saida.append("\t" + strCtipo);

            if (tipoVariavel == TipoDeclaracao.PONTEIRO){
                saida.append("*");
            }

            saida.append(" " + ctx.identificador(0).getText());

            escopo.escopoAtual().adicionar(ctx.identificador(0).IDENT(0).getText(), tipoVariavel);

            if (tipoVariavel == TipoDeclaracao.LITERAL){
                saida.append("[80]");
            }
            
            for (int i = 0; i < ctx.VIRGULA().size(); i++){
                saida.append(", " + ctx.identificador(i + 1).getText());
                escopo.escopoAtual().adicionar(ctx.identificador(i + 1).IDENT(0).getText(), tipoVariavel);

                if (tipoVariavel == TipoDeclaracao.LITERAL){
                    saida.append("[80]");
                }
            }
        }
        else if (tipoVariavel == TipoDeclaracao.REGISTRO){
            escopo.criarNovoEscopo();
            saida.append("\tstruct {\n");
            visitRegistro(ctx.tipo().registro());
            saida.append("\t} " + ctx.identificador(0).getText());

            TabelaDeSimbolos dadosRegistro = escopo.escopoAtual();

            escopo.escopoAtual().adicionarRegistro(ctx.identificador(0).getText(), dadosRegistro);
        }
        else {
            String nomeTipo = ctx.tipo().getText();
            TabelaDeSimbolos dadosRegistro = escopo.escopoAtual().recuperarRegistro(nomeTipo);

            saida.append("\t" + nomeTipo);
            saida.append(" " + ctx.identificador(0).getText());
            escopo.escopoAtual().adicionarRegistro(ctx.identificador(0).getText(), dadosRegistro);
            
            for (int i = 0; i < ctx.VIRGULA().size(); i++){
                saida.append(", " + ctx.identificador(i + 1).getText());
                escopo.escopoAtual().adicionarRegistro(ctx.identificador(i + 1).getText(), dadosRegistro);
            }
        }
        saida.append(";\n");

        return null;
    }

    @Override
    public Void visitRegistro(LAParser.RegistroContext ctx){
        ctx.variavel().forEach(var -> {
            saida.append("\t");
            visitVariavel(var);
        });
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        TipoDeclaracao tipoVariavel = LASemanticoUtils.verificarTipo(escopo, ctx.identificador());

        saida.append("\t");

        if (tipoVariavel == TipoDeclaracao.LITERAL){
            saida.append("strcpy(" + ctx.identificador().getText() + ", ");
            visitExpressao(ctx.expressao());
            saida.append(")");
        }
        else{
            if (ctx.PONTEIRO() != null){
                saida.append("*");
            }
            
            saida.append(ctx.identificador().getText() + " = ");
            visitExpressao(ctx.expressao());
        }

        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {

        saida.append("\tif ("); 
        visitExpressao(ctx.expressao()); 
        saida.append(") {\n");

        for (CmdContext cmdCtx: ctx.cmdIf)
        {
            saida.append("\t");
            visitCmd(cmdCtx);
        }

        saida.append("\t}\n");

        if (ctx.ELSE() != null){
            saida.append("\telse {\n");

            for (CmdContext cmdCtx : ctx.cmdElse)
            {
                saida.append("\t");
                visitCmd(cmdCtx);
            }

            saida.append("\t}\n");
        }

        return null;
    }

    @Override
    public Void visitOp_relacional(LAParser.Op_relacionalContext ctx) {
        if (ctx.IGUAL() != null) {
            saida.append(" == ");
        }
        else if (ctx.DIFERENTE() != null) {
            saida.append(" != ");
        }
        else if (ctx.MAIORIGUAL() != null) {
            saida.append(" >= ");
        }
        else if (ctx.MENORIGUAL() != null) {
            saida.append(" <= ");
        }
        else if (ctx.MAIOR() != null) {
            saida.append(" > ");
        }
        else if (ctx.MENOR() != null) {
            saida.append(" < ");
        }
        return null;
    }

    @Override
    public Void visitOp_logico_1(LAParser.Op_logico_1Context ctx){
        saida.append(" || ");
        return null;
    }

    @Override
    public Void visitOp_logico_2(LAParser.Op_logico_2Context ctx){
        saida.append(" && ");
        return null;
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        Iterator<IdentificadorContext> identificador = ctx.identificador().iterator();
        while(identificador.hasNext()){
            String nomeVar = identificador.next().getText();
            TipoDeclaracao tipoVar = escopo.escopoAtual().verificar(nomeVar);
            String formatString = LAGeradorUtils.TipoParaFormatString(tipoVar);

            if (formatString == "%s"){
                formatString = "%[^\\n]";
            }
            else{
                nomeVar = "&" + nomeVar;
            }

            saida.append("\tscanf(\"" + formatString + "\", " + nomeVar + ");\n\n");
        }
        
        return null;
    }

    @Override
    public Void visitExpressao(LAParser.ExpressaoContext ctx){
        visitTermo_logico(ctx.termo_logico(0));
        if (ctx.op_logico_1() != null){
            for (int i=0; i<ctx.op_logico_1().size(); i++){
                visitOp_logico_1(ctx.op_logico_1(i));
                visitTermo_logico(ctx.termo_logico(i+1));
            }
        }
        return null;
    }

    @Override
    public Void visitTermo_logico(LAParser.Termo_logicoContext ctx){
        visitFator_logico(ctx.fator_logico(0));
        if (ctx.op_logico_2() != null){
            for (int i=0; i<ctx.op_logico_2().size(); i++){
                visitOp_logico_2(ctx.op_logico_2(i));
                visitFator_logico(ctx.fator_logico(i+1));
            }
        }
        return null;
    }

    @Override
    public Void visitFator_logico(LAParser.Fator_logicoContext ctx){
        if (ctx.NOT() != null){
            saida.append("!(");
        }
        visitParcela_logica(ctx.parcela_logica());
        if (ctx.NOT() != null){
            saida.append(")");
        }
        return null;
    }

    @Override
    public Void visitParcela_logica(LAParser.Parcela_logicaContext ctx){
        if (ctx.TRUE() != null){
            saida.append("1");
        }
        else if (ctx.FALSE() != null){
            saida.append("0");
        }
        else{
            visitExp_relacional(ctx.exp_relacional());
        }
        return null;
    }
    
    @Override
    public Void visitExp_relacional(LAParser.Exp_relacionalContext ctx){
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null){
            visitOp_relacional(ctx.op_relacional());
            visitExp_aritmetica(ctx.exp_aritmetica(1));
        }
        return null;
    }

    @Override
    public Void visitExp_aritmetica(Exp_aritmeticaContext ctx) {
        visitTermo(ctx.termo(0));

        for (int i = 0; i < ctx.op1().size(); i++){
            saida.append(ctx.op1(i).getText());
            visitTermo(ctx.termo(i + 1));
        }

        return null;
    }

    @Override
    public Void visitTermo(TermoContext ctx) {
        visitFator(ctx.fator(0));

        for (int i = 0; i < ctx.op2().size(); i++){
            saida.append(ctx.op2(i).getText());
            visitFator(ctx.fator(i + 1));
        }

        return null;
    }

    @Override
    public Void visitFator(FatorContext ctx) {
        visitParcela(ctx.parcela(0));

        for (int i = 0; i < ctx.op3().size(); i++){
            saida.append(ctx.op3(i).getText());
            visitParcela(ctx.parcela(i + 1));
        }

        return null;
    }

    @Override
    public Void visitParcela(ParcelaContext ctx) {
        if (ctx.op_unario() != null){
            saida.append(ctx.op_unario().getText());
        }

        if (ctx.parcela_unario() != null){
            visitParcela_unario(ctx.parcela_unario());
        }
        else{
            saida.append(ctx.parcela_nao_unario().getText());
        }

        return null;
    }

    @Override
    public Void visitParcela_unario(Parcela_unarioContext ctx) {
        if (ctx.expressao() != null){
            visitExpressao(ctx.expressao());
        }
        else{
            saida.append(ctx.getText());
        }

        return null;
    }

    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        saida.append("\tprintf(\"");

        for (ExpressaoContext expressao: ctx.expressao()){
            TipoDeclaracao tipoExpressao = LASemanticoUtils.verificarTipo(escopo, expressao);

            if (expressao.getText().contains("\"")){
                saida.append(expressao.getText().replace("\"", ""));
            }
            else{
                LAGeradorUtils.adicionarVariavel(expressao.getText());
                String formatoString = LAGeradorUtils.TipoParaFormatString(tipoExpressao);
                saida.append(formatoString);
            }
        }

        // Adiciona aspas para fechar a string.
        saida.append("\"");

        Iterator<String> itLista = LAGeradorUtils.getIteratorLista();
        
        // Caso exista variáveis para serem mostradas.
        if (itLista.hasNext()){
            saida.append(", ");

            while (itLista.hasNext()){
                saida.append(itLista.next());
                
                if (itLista.hasNext()){
                    saida.append(", ");
                }
            }
        }

        saida.append(");\n");

        LAGeradorUtils.limparListaVariavel();

        return null;
    }

    @Override
    public Void visitCmdCaso(LAParser.CmdCasoContext ctx){
        saida.append("\tswitch (" + ctx.exp_aritmetica().getText() + ")\n\t{\n");
        visitSelecao(ctx.selecao());
        if (ctx.ELSE() != null){
            saida.append("\t\tdefault: \n");
            for (int i=0; i< ctx.cmd().size(); i++)
            {
                saida.append("\t");
                visitCmd(ctx.cmd(i));
            }
        }
        saida.append("\t}\n");
        return null;
    }

    @Override
    public Void visitSelecao(LAParser.SelecaoContext ctx){
        ctx.item_selecao().forEach(item -> visitItem_selecao(item));
        return null;
    }

    @Override
    public Void visitItem_selecao(LAParser.Item_selecaoContext ctx){
        for (Numero_intervaloContext ctxNumInterv: ctx.constantes().numero_intervalo()){
            int op_inicio = 1;
            int op_fim = 1;
            int intervalo_inicio = Integer.parseInt(ctxNumInterv.inicio.getText());
            int intervalo_fim = intervalo_inicio;

            if (ctxNumInterv.fim != null){
                intervalo_fim = Integer.parseInt(ctxNumInterv.fim.getText());
            }

            // Caso exista os operadores, inverte o sinal;
            if (ctxNumInterv.op_inicio != null)
                op_inicio = -1;

            if (ctxNumInterv.op_fim != null)
                op_fim = -1;

            // Caso não haja fim, o operador de inicio deve ser o mesmo que o fim.
            if (intervalo_inicio == intervalo_fim){
                op_fim = op_inicio;
            }

            intervalo_inicio = intervalo_inicio * op_inicio;
            intervalo_fim = intervalo_fim * op_fim;

            // Caso haja inicio e fim, coloca vários cases. 
            for(int i = intervalo_inicio; i < intervalo_fim; i++){
                saida.append("\t\tcase " + i + ":\n");
            }

            saida.append("\t\tcase " + intervalo_fim + ":\n");
            
            for (int j=0; j< ctx.cmd().size(); j++)
            {
                saida.append("\t");
                visitCmd(ctx.cmd(j));
            }
        }
        saida.append("\t\tbreak;\n\n");
        return null;
    }

    @Override
    public Void visitCmdPara(CmdParaContext ctx) {
        saida.append("\n\tfor (" + ctx.IDENT().getText() + " = " + ctx.inicioExp.getText() + "; " + ctx.IDENT().getText() + " <= " + ctx.fimExp.getText() + "; " + ctx.IDENT().getText() + "++) {\n");

        for (CmdContext cmdCtx: ctx.cmd()){
            saida.append("\t");
            visitCmd(cmdCtx);
        }

        saida.append("\t}\n\n");

        return null;
    }

    @Override
    public Void visitCmdEnquanto(CmdEnquantoContext ctx) {
        saida.append("\n\twhile (");
        visitExpressao(ctx.expressao());
        saida.append(") {\n");

        for (CmdContext cmdCtx: ctx.cmd()){
            saida.append("\t");
            visitCmd(cmdCtx);
        }

        saida.append("\t}\n\n");

        return null;
    }

    @Override
    public Void visitCmdFaca(CmdFacaContext ctx) {
        saida.append("\n\tdo {\n");

        for (CmdContext cmdCtx: ctx.cmd()){
            saida.append("\t");
            visitCmd(cmdCtx);
        }

        saida.append("\t} while (");
        visitExpressao(ctx.expressao());
        saida.append(");\n\n");

        return null;
    }

    @Override
    public Void visitCmdChamada(CmdChamadaContext ctx) {
        saida.append("\t" + ctx.IDENT().getText() + "(");

        visitExpressao(ctx.expressao(0));

        saida.append(");\n");

        return null;
    }

    @Override
    public Void visitCmdRetorne(CmdRetorneContext ctx) {
        saida.append("\treturn ");
        visitExpressao(ctx.expressao());
        saida.append(";\n");

        return null;
    }
}
