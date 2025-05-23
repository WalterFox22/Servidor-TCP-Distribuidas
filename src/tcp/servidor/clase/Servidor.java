package tcp.servidor.clase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Servidor {
    private static final String RUTA_ARCHIVO = "C:/Users/Soledad Cobacango/OneDrive - Escuela Politécnica Nacional/PC/QUINTO SEMESTRE/Aplicaciones Distribuidas/Otros/registro_clientes.txt";
    private static Map<String, List<String>> registros = cargarRegistros();

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

                String mensaje = dis.readUTF();
                if (mensaje.equals("x")) break;

                // Separar nombre y acción
                String[] partes = mensaje.split(" - ", 2);
                String nombre = partes[0];
                String accion = partes.length > 1 ? partes[1] : "Acción desconocida";

                // Generar registro
                String registro = accion + " (" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + ")";

                // Añadir al historial del usuario
                registros.computeIfAbsent(nombre, k -> new ArrayList<>()).add(registro);

                // Guardar en archivo
                guardarRegistros();

                // Preparar respuesta
                StringBuilder historial = new StringBuilder("Historial de " + nombre + ":\n");
                for (String r : registros.get(nombre)) {
                    historial.append("- ").append(r).append("\n");
                }

                dos.writeUTF(historial.toString());
            } catch (IOException e) {
                System.err.println("Error al procesar la solicitud: " + e.getMessage());
            }
        }
        servidor.close();
    }

    private static void guardarRegistros() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(registros);
            System.out.println("Registros guardados correctamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar registros: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> cargarRegistros() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Map<String, List<String>>) ois.readObject();
        } catch (Exception e) {
            System.err.println("No se pudo cargar el archivo de registros: " + e.getMessage());
            return new HashMap<>();
        }
    }
}