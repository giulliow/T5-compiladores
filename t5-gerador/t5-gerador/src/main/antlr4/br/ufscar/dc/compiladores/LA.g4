grammar LA;

// Parte Léxica (T1)
// -----------------

// Máquina de estados que verifica o comentário corretamente, em que se inicia com o '{' aceita qualquer 
// caracter dentro deste, sem ter quebra de linha e finaliza com '}'.
COMENTARIO       : '{' ~('\n')*? '}' {skip();};

// Palavras chaves.
ALGORITMO        : 'algoritmo';
FIM_ALGORITMO    : 'fim_algoritmo';
DECLARE          : 'declare';
CONSTANTE        : 'constante';
LITERAL          : 'literal';
INTEIRO          : 'inteiro';
REAL             : 'real';
LOGICO           : 'logico';
TRUE             : 'verdadeiro';
FALSE            : 'falso';
AND              : 'e';
OR               : 'ou';
NOT              : 'nao';
IF               : 'se';
THEN             : 'entao';
ELSE             : 'senao';
ENDIF            : 'fim_se';
CASO             : 'caso';
SEJA             : 'seja';
FIM_CASO         : 'fim_caso';
PARA             : 'para';
ATE              : 'ate';
FACA             : 'faca';
FIM_PARA         : 'fim_para';
WHILE            : 'enquanto';
ENDWHILE         : 'fim_enquanto';
TIPO             : 'tipo';
REGISTRO         : 'registro';
FIM_REGISTRO     : 'fim_registro';
PROCEDIMENTO     : 'procedimento';
VAR              : 'var';
FIM_PROCEDIMENTO : 'fim_procedimento';
FUNCAO           : 'funcao';
RETORNE          : 'retorne';
FIM_FUNCAO       : 'fim_funcao';
LEIA             : 'leia';
ESCREVA          : 'escreva';

// Intervalo de valores.
INTERVALO        : '..';

// Operadores Relacionais.
MENOR            : '<';
MENORIGUAL       : '<=';
MAIOR            : '>';
MAIORIGUAL       : '>=';
IGUAL            : '=';
DIFERENTE        : '<>';

// Delimitadores.
DELIM            : ':';
ABREPAR          : '(';
FECHAPAR         : ')';
ABRECHAVE        : '[';
FECHACHAVE       : ']';
VIRGULA          : ',';
ASPAS            : '"';

// Operadores aritméticos.
DIVISAO          : '/';
MOD              : '%';
SOMA             : '+';
SUBTRACAO        : '-';
MULTIPLICACAO    : '*';

// Operadores de manipulação de memória.
ATRIBUICAO       : '<-';
PONTEIRO         : '^';
ENDERECO         : '&';
PONTO            : '.';

// Números.
NUM_INT          : ('0'..'9')+;
NUM_REAL         : ('0'..'9')+ ('.' ('0'..'9')+)?;

// Identificadores.
// Identificadores começam com qualquer letra, maiúscula 
// ou minuscula, seguida de qualquer letra, ou digito, ou '_' 
IDENT            : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

// Cadeia de string.
// Cadeias de strings são iniciadas por '"', seguidos por quaisquer caracter, sem 
// ser o caracter '\n' e finalizados por '"'.
CADEIA           : '"' ( ~('\n') )*? '"';

// Erro de cadeia de string
// Verifica qualquer cadeia que não foi fechada. Deve vir abaixo da CADEIA
// pois senão pode gerar conflito de nunca encontrar a cadeia.
CADEIA_N_FECHADA : '"' ( ~('\n'|'"') )*? '\n';

// Espaço em branco.
WS               : ( ' ' | '\t' | '\r' | '\n' ) {skip();};

// Erro de comentário não fechado.
COMENT_N_FECHADO : '{' ~('\n'|'}')*? '\n';

// Caso não for identificado nenhuma regra acima, gera um erro.
ERRO             : .;

// Parte Sintática (T2)
// ------------------------------------------------

// Regra de definição do espaço do algorítmo, que se inicia após as declarações de variáveis.
programa
    : declaracoes 'algoritmo' corpo 'fim_algoritmo'
    ;

// Regra de definição dos diferentes tipos de declarações de variáveis e funções.
declaracoes
    : (declaracao_variaveis | declaracao_funcoes)*
    ;

// Regra de declaração de variáveis.
declaracao_variaveis
    : 'declare' variavel 
    | 'constante' IDENT ':' tipo_basico '=' valor_constante 
    | 'tipo' IDENT ':' registro
    ;
variavel
    : identificador (',' identificador)* ':' tipo
    ;

// Regra de declaração de um vetor.
identificador
    : IDENT ('.' IDENT)* ('[' exp_aritmetica ']')*
    ;

// Regra de declaração de variáveis "complexas", como "estruturas" e "ponteiros".
tipo
    : registro 
    | tipo_variavel
    ;

// Regra de declaração dos tipos básicos de variáveis.
tipo_basico
    : LITERAL
    | INTEIRO 
    | REAL 
    | LOGICO
    ;

// Regra de declaração de um ponteiro.
tipo_variavel
    : '^'? (tipo_basico | IDENT)
    ;

// Regra de declaração de valores constantes.
valor_constante
    : CADEIA 
    | NUM_INT 
    | NUM_REAL 
    | TRUE 
    | FALSE
    ;    

// Regra de declaração de "estrutura de dados".
registro
    : 'registro' variavel* 'fim_registro'
    ;

// Regra de declaração de parâmetros de uma função.
parametro
    : 'var'? identificador (',' identificador)* ':' tipo_variavel
    ;

parametros
    : parametro (',' parametro)*
    ;

// Regra de declaração de funções e procedimentos.
declaracao_funcoes
    : 'procedimento' IDENT '(' parametros? ')' declaracao_variaveis* cmd* 'fim_procedimento' 
    | 'funcao' IDENT '(' parametros? ')' ':' tipo_variavel declaracao_variaveis* cmd* 'fim_funcao'
    ;

// Regra de definição do corpo de uma função ou procedimento.
corpo
    : declaracao_variaveis* cmd*
    ;

// Regras auxiliares para definição de seleção do valor para o comando "Caso".
selecao
    : item_selecao*
    ;
item_selecao
    : constantes ':' cmd*
    ;
constantes
    : numero_intervalo (',' numero_intervalo)*
    ;
numero_intervalo
    : op_inicio=op_unario? inicio=NUM_INT ('..' op_fim=op_unario? fim=NUM_INT)?
    ;

// Regra para definir se o número da regra do comando "Caso" é negativo.
op_unario
    : '-'
    ;

// Regras de definição de operações aritméticas, separadas em grupo para definir a ordem de prioridades das operações.
exp_aritmetica
    : termo (op1 termo)*
    ;
termo
    : fator (op2 fator)*
    ;
fator
    : parcela (op3 parcela)*
    ;
op1
    : '+' | '-'
    ;
op2
    : '*' | '/'
    ;
op3
    : '%'
    ;

// Regras de definição de expressões aritméticas, com variáveis, ou números constantes. 
parcela
    : op_unario? parcela_unario | parcela_nao_unario
    ;
parcela_unario
    : '^'? identificador
	| cmdChamada
	| NUM_INT
	| NUM_REAL
	| '(' exp_unica=expressao ')'
    ;

// Regra de definição de recuperação do valor de um endereço da variável.
parcela_nao_unario
    : '&' identificador | CADEIA
    ;

// Regras de definição de expressões relacionais.
exp_relacional
    : exp_aritmetica (op_relacional exp_aritmetica)?
    ;
op_relacional
    : '=' | '<>' | '>=' | '<=' | '>' | '<'
    ;
expressao
    : termo_logico (op_logico_1 termo_logico)*
    ;
termo_logico
    : fator_logico (op_logico_2 fator_logico)*
    ;
fator_logico
    : 'nao'? parcela_logica
    ;
parcela_logica
    : ( 'verdadeiro' | 'falso' )
	| exp_relacional
    ;
op_logico_1
    : 'ou'
    ;
op_logico_2
    : 'e'
    ;

// Regras de definições de comandos da linguagem.
cmd
    : cmdLeia 
    | cmdEscreva 
    | cmdSe 
    | cmdCaso 
    | cmdPara 
    | cmdEnquanto 
    | cmdFaca 
    | cmdAtribuicao 
    | cmdChamada 
    | cmdRetorne
    ;
cmdLeia
    : 'leia' '(' '^'? identificador (',' '^'? identificador)* ')'
    ;
cmdEscreva
    : 'escreva' '(' expressao (',' expressao)* ')'
    ;
cmdSe
    : 'se' expressao 'entao' cmdIf+=cmd* ('senao' cmdElse+=cmd*)? 'fim_se'
    ;
cmdCaso
    : 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso'
    ;
cmdPara
    : 'para' IDENT '<-' inicioExp=exp_aritmetica 'ate' fimExp=exp_aritmetica 'faca' cmd* 'fim_para'
    ;
cmdEnquanto
    : 'enquanto' expressao 'faca' cmd* 'fim_enquanto'
    ;
cmdFaca
    : 'faca' cmd* 'ate' expressao
    ;
cmdAtribuicao
    : '^'? identificador '<-' expressao
    ;
cmdChamada
    : IDENT '(' expressao (',' expressao)* ')'
    ;
cmdRetorne
    : 'retorne' expressao
    ;
