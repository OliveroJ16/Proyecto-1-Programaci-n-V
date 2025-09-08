import java.util.Random;

public class Sistema {
    public static void main(String[] args) {

        Random random = new Random();
        
        final int cambioContexto = 1; //Establecer el cambio de contexto
        final int numeroColas = 2; //Establecer el numero de colas
        final int quantum;
        final int numeroProcesos;

        quantum = 4;//random.nextInt(91) + 10;  // quantum entre 10 y 100
        numeroProcesos = random.nextInt(31); // N procesos entre 0 - 30
        
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


    }
}