import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Sistema {
    public static void main(String[] args) {

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);

        final int cambioContexto = 1;
        final int quantum;
        final int numeroProcesos;

        quantum = 4;//random.nextInt(91) + 10;

        System.out.print("Ingrese el numero de procesos: ");
        numeroProcesos = scanner.nextInt();

        SimuladorMLQ simulador = new SimuladorMLQ(cambioContexto, quantum, numeroProcesos);

        System.out.println("Quantum: " + quantum);
        System.out.println("CDC: " + cambioContexto);
        System.out.println();

        simulador.crearProcesos();

        mostrarProcesosIniciales(simulador.obtenerProcesos());

        List<String> logs = simulador.ejecutarSimulacion();
        mostrarLogsPaginados(logs);

        GestorArchivo gestor = new GestorArchivo("salida_simulacion.txt");
        gestor.guardarResultados(logs);

        simulador.mostrarMetricas();
    }

    private static void mostrarProcesosIniciales(List<Proceso> procesos) {
        System.out.printf("%-4s %-4s %-5s %-6s %-3s %-7s %-5s%n",
                "Proc", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "TiempoBlock");

        for (Proceso p : procesos) {
            System.out.printf("P%-3d %-4d %-5d %-6s %-3d %-7s %-5d%n",
                    p.getIdProceso(),
                    p.getIdCola(),
                    p.getCantidadInstrucciones(),
                    p.getEstado(),
                    p.getTiempoCambioContexto(),
                    p.isRequiereBloqueo() ? "Si" : "No",
                    p.getTiempoBloqueado());
        }
        System.out.println();
    }

    private static void mostrarLogsPaginados(List<String> logs) {
        try (Scanner scanner = new Scanner(System.in)) {
            int contador = 0;
            for (String salida : logs) {
                contador++;
                System.out.println(salida);

                if (contador % 3 == 0 && contador < logs.size()) {
                    System.out.println("Presiona ENTER para continuar...");
                    scanner.nextLine();
                }
            }
        }
    }
}
