## Seguranca e Confiabilidade 2025/2026
# SegC-grupo26proj1-fase1
## Grupo SEGC-026
 - Irina Santos fc59786
 - Gonçalo Costa fc61803
 - Beatriz Beate fc62356

Projeto Java com código fonte em `src/` e binários compilados em `bin/`.

## Compilar

### Linux

Dar permissões de execução aos scripts na primeira vez:

```sh
chmod +x compile.sh run-server.sh run-client.sh
```

Compilar:

```sh
./compile.sh
```

### Windows

Em PowerShell:

```powershell
Preencham sff
```


## Correr o servidor

### Linux

```sh
./run-server.sh
```

Porto específico:

```sh
./run-server.sh 25000
```

### Windows

Em PowerShell, porto por omissão:

```powershell
java -cp bin server.SpertaServer
```

Em PowerShell, porto específico:

```powershell
java -cp bin server.SpertaServer 25000
```


## Correr o cliente

### Linux

```sh
./run-client.sh
```

Porto específico:

```sh
./run-client.sh 25000
```

### Windows

Em PowerShell:

```powershell
java -cp bin client.SpertaClient localhost user1 12345
```

Em PowerShell, porto específico:

```powershell
java -cp bin client.SpertaClient localhost:25000 user1 12345
```
