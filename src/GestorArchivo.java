import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class GestorArchivo {

    private final String nombreArchivo;

    public GestorArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    // Guarda todo el log de la simulación
    public void guardarResultados(List<String> logs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            for (String salida : logs) {
                writer.println(salida);
            }
            System.out.println("Resultados guardados en: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar resultados en archivo: " + e.getMessage());
        }
    }
}
