# T5 Compiladores

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

### Integrantes

- Jorge Pires (790942)
- Giullio Gerolamo (790965)

## Gerador de Código para a Linguagem LA
Este é o trabalho 5 (T5) da disciplina, que consiste na implementação de um gerador de código para a linguagem LA (Linguagem Algorítmica) desenvolvida pelo prof. Jander, no âmbito do DC/UFSCar. O gerador de código deverá produzir código executável em C equivalente ao programa de entrada. Exemplo:

**Entrada:**
```
algoritmo
  declare
    x: literal
  leia(x)
  escreva(x)
fim_algoritmo
```


**Saída produzida:**
```
#include <stdio.h>
#include <stdlib.h>
int main() {
	char x[80];
	gets(x);
	printf("%s",x);
	return 0;
}
```

**IMPORTANTE:** o código gerado não precisa ser idêntico ao fornecido nos casos de teste, mas ele deve ser compilável usando GCC, e sua EXECUÇÃO deve ser a mesma que a dos casos de teste. O corretor automático irá compilar o código gerado (usando GCC), executá-lo e comparar a entrada/saída com o que é esperado.

## Como executar o gerador

### Dependências utilizadas
- Java: 1.8
- Junit: 4.11
- Antlr: 4.11.1
- maven-clean-plugin: 3.1.0
- maven-resources-plugin: 3.0.2
- maven-compiler-plugin: 3.8.0
- maven-surefire-plugin: 2.22.1
- maven-jar-plugin: 3.0.2
- maven-install-plugin: 2.5.2
- maven-deploy-plugin: 2.8.2
- maven-site-plugin: 3.7.1
- maven-project-info-reports-plugin: 3.0.0
  
### Execução do Gerador
```
mvn clean package
```
    
```
 java -jar target/t5-gerador-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho para o código fonte LA> [caminho para arquivo de saída]
```
    
- ``caminho_entrada``: Caminho completo do arquivo contendo o programa-fonte em linguagem LA.
