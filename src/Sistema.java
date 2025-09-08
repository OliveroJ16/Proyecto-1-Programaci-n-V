import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Sistema {
    public static void main(String[] args) {

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        
        final int cambioContexto = 1; //Establecer el cambio de contexto
        final int numeroColas = 2; //Establecer el numero de colas
        final int quantum;
        final int numeroProcesos;

        quantum = random.nextInt(91) + 10;  // quantum entre 10 y 100
        //numeroProcesos = random.nextInt(31); // N procesos entre 0 - 30
        numeroProcesos = 8; //Cambiar despues
        
        SimuladorMLQ simulador = new SimuladorMLQ(cambioContexto, quantum, numeroProcesos);
        
        //Mostrar datos iniciales
        System.out.println("Quantum: " + quantum);
        System.out.println("CDC: " + cambioContexto);
        System.out.println();

        simulador.crearProcesos();
        
        System.out.printf("%-4s %-4s %-5s %-6s %-3s %-7s %-5s%n", 
    "Proc", "Cola", "Inst.", "Estado", "CDC", "Bloqueo", "TiempoBlock");
        for (Proceso p : simulador.obtenerProcesos()) {
            System.out.printf("P%-4d %-4d %-5d %-6s %-3d %-7s %-5d%n",
                p.getIdProceso(),
                p.getIdCola(),
                p.getCantidadInstrucciones(),
                p.getEstado(),
                p.getTiempoCambioContexto(),
                p.isRequiereBloqueo() ? "Si" : "No",
                p.getTiempoBloqueado()
            );
        }
        //Iniciar simulacion
        int contador = 0;
        List<String> logs = simulador.ejecutarSimulacion();
        for(String salida : logs){
            contador ++;
            System.out.println(salida);
        
            if (contador % 10 == 0) {
                System.out.println("Presiona ENTER para continuar...");
                scanner.nextLine();
            }
        }

        scanner.close();

        GestorArchivo gestor = new GestorArchivo("salida_simulacion.txt");
        gestor.guardarResultados(logs);
    }
}