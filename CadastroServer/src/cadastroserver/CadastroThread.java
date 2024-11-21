/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cadastroserver;

import Model.Usuario;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 *
 * @author Temis
 */
public class CadastroThread extends Thread{
    
    private final ProdutoJpaController ctrl;
    private final UsuarioJpaController ctrlUsu;
    private final Socket s1;

    public CadastroThread(ProdutoJpaController ctrl, UsuarioJpaController ctrlUsu, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.s1 = s1;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s1.getInputStream())
        ) {
            System.out.println("Cliente conectado.");

            String login = (String) in.readObject();
            String senha = (String) in.readObject();

            Usuario usuario = ctrlUsu.findUsuario(login, senha);

            if (usuario == null) {
                out.writeObject("Credenciais inválidas. Desconectando.");
                s1.close();
                return;
            } else {
                out.writeObject("Login bem-sucedido. Bem-vindo!");
            }

            boolean conectado = true;
            while (conectado) {
                String comando = (String) in.readObject();

                if ("L".equalsIgnoreCase(comando)) {
                    List<?> produtos = ctrl.findProdutos(); // Substitua por método apropriado para listar produtos
                    out.writeObject(produtos);
                } else if ("S".equalsIgnoreCase(comando)) {
                    out.writeObject("Desconectando. Até logo!");
                    conectado = false;
                } else {
                    out.writeObject("Comando não reconhecido.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            try {
                s1.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket: " + e.getMessage());
            }
            System.out.println("Conexão com o cliente encerrada.");
        }
    }
}
