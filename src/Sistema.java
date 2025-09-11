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

        quantum = random.nextInt(91) + 10;
        System.out.print("Ingrese el numero de procesos: ");
        numeroProcesos = scanner.nextInt();

        SimuladorMLQ simulador = new SimuladorMLQ(cambioContexto, quantum, numeroProcesos);

        System.out.println("Quantum: " + quantum);
        System.out.println("CDC: " + cambioContexto);
        System.out.println();

        simulador.crearProcesos();

        System.out.println("=== PCBs iniciales ===");
        mostrarPCBsIniciales(simulador.obtenerProcesos());

        System.out.println("=== Tabla inicial de estados ===");
        mostrarTablaInicial(simulador.obtenerProcesos());

        List<String> logs = simulador.ejecutarSimulacion();
        mostrarLogsPaginados(logs);

        simulador.mostrarMetricas();

        scanner.close();
    }

    private static void mostrarPCBsIniciales(List<Proceso> procesos) {
        System.out.printf("%-6s %-6s %-8s %-8s %-6s %-8s %-8s%n",
                "Proc", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "TiempoBlock");

        for (Proceso p : procesos) {
            System.out.printf("P%-5d %-6d %-8d %-8s %-6d %-8s %-8d%n",
                    p.getIdProceso(),
                    p.getIdCola(),
                    p.getCantidadInstrucciones(),
                    p.getEstado(),
                    p.getTiempoCambioContexto(),
                    p.isRequiereBloqueo() ? "Si" : "No",
                    p.getTiempoBloqueoDefinido());
        }
        System.out.println();
    }

    private static void mostrarTablaInicial(List<Proceso> procesos) {
        System.out.printf("%-6s %-8s %-8s %-6s %-6s %-6s %-6s%n",
                "Proc", "Inst.", "Estado", "Cola", "CDC", "Block", "Exe");
        for (Proceso p : procesos) {
            System.out.printf("P%-5d %-8d %-8s %-6d %-6d %-6d %-6d%n",
                    p.getIdProceso(),
                    p.getCantidadInstrucciones(),
                    "L", // todos inician en Listo
                    p.getTiempoEnCola(),
                    p.getTiempoCambioContexto(),
                    p.getTiempoBloqueado(),
                    p.getTiempoEjecucion());
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
