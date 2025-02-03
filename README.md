# ARSW-LAB02-PART2

SnakeRace - Mejoras de Concurrencia y Estabilidad
Este documento describe las mejoras realizadas en el código del juego SnakeRace para garantizar un comportamiento correcto en un entorno multihilo. Se abordaron problemas de concurrencia, condiciones de carrera, uso inadecuado de colecciones y esperas activas innecesarias.

Cambios realizados
1. Eliminación de esperas activas
   El código original utilizaba un bucle de espera activa en el método init de la clase SnakeApp para verificar si todas las serpientes habían terminado. Este enfoque consume recursos de la CPU innecesariamente.

Solución:
Se reemplazó el bucle de espera activa con un CountDownLatch. Este mecanismo permite que el hilo principal espere a que todos los hilos de las serpientes terminen de manera eficiente.

Cambios en el código:
Se añadió un CountDownLatch en la clase SnakeApp.

Se modificó el método init para usar latch.await() en lugar del bucle de espera activa.

Se pasó el CountDownLatch a cada instancia de Snake para que notifique cuando termine.

java
Copy
// En SnakeApp
private CountDownLatch latch = new CountDownLatch(MAX_THREADS);

private void init() {
for (int i = 0; i != MAX_THREADS; i++) {
snakes[i] = new Snake(i + 1, spawn[i], i + 1, latch); // Pasar el latch
snakes[i].addObserver(board);
thread[i] = new Thread(snakes[i]);
thread[i].start();
}

    frame.setVisible(true);

    try {
        latch.await(); // Esperar a que todas las serpientes terminen
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

// En Snake
@Override
public void run() {
while (!snakeEnd) {
snakeCalc();
setChanged();
notifyObservers();
try {
Thread.sleep(hasTurbo ? 500 / 3 : 500);
} catch (InterruptedException e) {
e.printStackTrace();
}
}
latch.countDown(); // Notificar que la serpiente ha terminado
}
2. Sincronización de recursos compartidos
   El código original accedía y modificaba recursos compartidos (como Board.gameboard, Board.turbo_boosts, Board.jump_pads, y Board.food) sin sincronización, lo que podía causar condiciones de carrera.

Solución:
Se sincronizó el acceso a estos recursos utilizando bloques synchronized y se cambiaron las colecciones no seguras para hilos por versiones seguras.

Cambios en el código:
Se reemplazó LinkedList<Cell> por CopyOnWriteArrayList<Cell> para snakeBody.

Se añadieron bloques synchronized para proteger el acceso a Board.gameboard, Board.turbo_boosts, Board.jump_pads, y Board.food.

java
Copy
// En Snake
private CopyOnWriteArrayList<Cell> snakeBody = new CopyOnWriteArrayList<>();

private void checkIfBarrier(Cell newCell) {
synchronized (Board.gameboard) { // Sincronizar acceso al tablero
if (Board.gameboard[newCell.getX()][newCell.getY()].isBarrier()) {
System.out.println("[" + idt + "] " + "CRASHED AGAINST BARRIER " + newCell.toString());
snakeEnd = true;
}
}
}

private void checkIfTurboBoost(Cell newCell) {
synchronized (Board.turbo_boosts) { // Sincronizar acceso a turbo_boosts
if (Board.gameboard[newCell.getX()][newCell.getY()].isTurbo_boost()) {
for (int i = 0; i != Board.NR_TURBO_BOOSTS; i++) {
if (Board.turbo_boosts[i] == newCell) {
Board.turbo_boosts[i].setTurbo_boost(false);
Board.turbo_boosts[i] = new Cell(-5, -5);
hasTurbo = true;
}
}
System.out.println("[" + idt + "] " + "GETTING TURBO BOOST " + newCell.toString());
}
}
}
3. Uso de colecciones seguras para hilos
   El código original utilizaba colecciones no seguras para hilos, como LinkedList y arrays estáticos, lo que podía causar inconsistencias en un entorno multihilo.

Solución:
Se cambiaron las colecciones no seguras por versiones seguras para hilos, como CopyOnWriteArrayList.

Cambios en el código:
Se reemplazó LinkedList<Cell> por CopyOnWriteArrayList<Cell> en la clase Snake.

java
Copy
// En Snake
private CopyOnWriteArrayList<Cell> snakeBody = new CopyOnWriteArrayList<>();