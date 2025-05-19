package tcp.servidor.clase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Servidor {
    public static String generarRespuesta(String mensaje) {
        Date date = new Date();
        DateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return mensaje + "\nFecha y Hora del registro: " + formato.format(date);
    }

    public static void procesarSolicitud(int puerto) throws Exception {
        ServerSocket servidor = new ServerSocket(puerto);
        System.out.println("Servidor corriendo en el puerto " + puerto);

        while (true) {
            Socket cliente = servidor.accept();
            System.out.println("Cliente conectado");

            try (InputStream in = cliente.getInputStream();
                 OutputStream out = cliente.getOutputStream();
                 DataInputStream dis = new DataInputStream(in);
                 DataOutputStream dos = new DataOutputStream(out)) {

                // Leer el mensaje del cliente
                String mensaje = dis.readUTF();
                if (mensaje.equals("x")) break;

                // Generar respuesta con fecha y hora
                String respuesta = generarRespuesta(mensaje);
                System.out.println("Mensaje recibido: " + mensaje);

                // Guardar en el archivo
                guardarEnArchivo(mensaje, respuesta, "C:/Users/APP DISTRIBUIDAS/IdeaProjects/registro_clientes.dat");

                // Enviar respuesta al cliente
                dos.writeUTF(respuesta);
            } catch (IOException e) {
                System.err.println("Error al procesar la solicitud: " + e.getMessage());
            }
        }
        servidor.close();
    }

    private static void guardarEnArchivo(String mensaje, String respuesta, String rutaArchivo) {
        String registro = "Mensaje del cliente: " + mensaje + "\nRespuesta: " + respuesta + "\nFecha: " + new Date() + "\n";
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo, true);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(registro);
            System.out.println("Registro guardado correctamente en: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar en el archivo: " + e.getMessage());
        }
    }


}