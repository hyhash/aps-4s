# 🌊 Sistema de Monitoramento do Rio Tietê
### APS 2025 – Ciência da Computação – UNIP

---

## ⚡ COMO EXECUTAR (Passo a Passo)

### Pré-requisito obrigatório
Instale o **JDK (Java Development Kit)** — versão 8 ou superior.
- Download: https://www.oracle.com/java/technologies/downloads/
- ⚠️ Precisa ser o **JDK**, não apenas o JRE!
- Após instalar, verifique no terminal/CMD: `javac -version`

---

### ▶️ No Windows

**1. Inicie o Servidor** (máquina da Secretaria):
```
Dê duplo clique em:  iniciar-servidor.bat
```

**2. Inicie o Cliente** (cada inspetor):
```
Dê duplo clique em:  iniciar-cliente.bat
```

---

### ▶️ No Linux / Mac

```bash
chmod +x iniciar-servidor.sh iniciar-cliente.sh
./iniciar-servidor.sh      # Terminal 1
./iniciar-cliente.sh       # Terminal 2
```

---

### ▶️ Compilar e executar manualmente

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/common/Message.java
javac -encoding UTF-8 -cp out -d out src/server/ServerGUI.java src/server/ClientHandler.java src/server/ChatServer.java
javac -encoding UTF-8 -cp out -d out src/client/ChatClient.java src/client/ClientGUI.java

java -cp out server.ChatServer   # Terminal 1
java -cp out client.ClientGUI    # Terminal 2
```

---

## 🖥️ Tela de Login do Cliente

| Campo    | Valor                                     |
|----------|-------------------------------------------|
| Servidor | `localhost` (mesma máquina) ou IP da rede |
| Porta    | `12345`                                   |
| Seu nome | Nome do inspetor (ex: `Inspetor Carlos`)  |

---

## 📁 Estrutura do Projeto

```
aps-chat/
├── src/
│   ├── common/
│   │   └── Message.java           # Mensagem serializada
│   ├── server/
│   │   ├── ChatServer.java        # Servidor TCP/IP principal
│   │   ├── ClientHandler.java     # Thread por cliente
│   │   └── ServerGUI.java         # Interface do servidor
│   └── client/
│       ├── ChatClient.java        # Lógica de conexão TCP
│       └── ClientGUI.java         # Interface do inspetor
├── iniciar-servidor.bat / .sh
├── iniciar-cliente.bat / .sh
└── README.md
```

---

## 🔧 Funcionalidades

- Chat em grupo (broadcast para todos os inspetores)
- Mensagem privada entre dois inspetores 🔒
- Transferência de arquivos (laudos, relatórios — até 5 MB)
- Painel de emojis temáticos 🌊🌿🐟⚠️🏭💧
- Lista de usuários online em tempo real
- Múltiplos clientes simultâneos
- Servidor funciona com ou sem interface gráfica

---

## 🔌 Arquitetura TCP/IP

```
[Inspetor A] ──TCP──┐
[Inspetor B] ──TCP──┼──► [ChatServer :12345] ──► clientes
[Inspetor C] ──TCP──┘
```

- Protocolo: TCP/IP (confiável, orientado a conexão)
- Sockets: Berkeley Sockets — java.net.Socket / ServerSocket
- Serialização: ObjectInputStream / ObjectOutputStream
- Concorrência: Thread por cliente (Runnable)
- Porta padrão: 12345

---

## 👥 Integrantes

| Nome | RA |
|------|----|
|      |    |
|      |    |
|      |    |

*Disciplina: Arquitetura de Redes de Computadores — UNIP 2025*
