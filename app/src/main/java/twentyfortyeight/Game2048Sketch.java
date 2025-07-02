// Processing Sketch Class
import processing.core.PApplet;
import processing.core.PFont;
import java.util.ArrayList;
import java.util.Random;

public class Game2048Sketch extends PApplet {
    private int gridSize;
    private int[][] grid;
    private int cellSize;
    private int boardSize;
    private int margin;
    private boolean gameOver;
    private long startTime;
    private long endTime;
    private Random random;
    private boolean animating;
    private ArrayList<Animation> animations;
    private boolean movePerformed;

    // Colors for different number blocks
    private int[] colors = {
        color(238, 228, 218), // 2
        color(237, 224, 200), // 4
        color(242, 177, 121), // 8
        color(245, 149, 99),  // 16
        color(246, 124, 95),  // 32
        color(246, 94, 59),   // 64
        color(237, 207, 114), // 128
        color(237, 204, 97),  // 256
        color(237, 200, 80),  // 512
        color(237, 197, 63),  // 1024
        color(237, 194, 46)   // 2048+
    };

    public Game2048Sketch(int gridSize) {
        this.gridSize = gridSize;
    }

    // Animation class for smooth transitions
    private class Animation {
        int fromX, fromY, toX, toY;
        int value;
        float progress;
        boolean merging;
        int mergeValue;
        
        Animation(int fromX, int fromY, int toX, int toY, int value, boolean merging, int mergeValue) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.value = value;
            this.progress = 0;
            this.merging = merging;
            this.mergeValue = mergeValue;
        }
        
        boolean isComplete() {
            return progress >= 1.0;
        }
        
        void update() {
            progress += 0.1;
            if (progress > 1.0) progress = 1.0;
        }
        
        float getCurrentX() {
            return fromX + (toX - fromX) * progress;
        }
        
        float getCurrentY() {
            return fromY + (toY - fromY) * progress;
        }
    }

    @Override
    public void settings() {
        size(600, 700);
    }

    @Override
    public void setup() {
        grid = new int[gridSize][gridSize];
        boardSize = 600;
        cellSize = boardSize / gridSize;
        margin = 10;
        gameOver = false;
        startTime = System.currentTimeMillis();
        random = new Random();
        animations = new ArrayList<>();
        animating = false;
        
        // Add two initial blocks
        addRandomBlock();
        addRandomBlock();
    }

    @Override
    public void draw() {
        background(250, 248, 239);
        
        // Draw the timer
        drawTimer();
        
        // Draw the grid and blocks
        drawGrid();
        
        if (animating) {
            animateBlocks();
        }
        
        // Check if game is over
        if (gameOver) {
            drawGameOver();
        }
    }

    private void drawTimer() {
        fill(119, 110, 101);
        textAlign(RIGHT, TOP);
        textSize(24);
        
        long currentTime;
        if (gameOver) {
            currentTime = endTime;
        } else {
            currentTime = System.currentTimeMillis();
        }
        
        long elapsedSeconds = (currentTime - startTime) / 1000;
        text("Time: " + elapsedSeconds + "s", width - 20, 20);
    }

    private void drawGrid() {
        // Draw background grid
        fill(187, 173, 160);
        rect(0, 100, boardSize, boardSize, 10);
        
        // Draw empty cells
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                fill(205, 193, 180);
                rect(j * cellSize + margin, i * cellSize + margin + 100, 
                     cellSize - 2 * margin, cellSize - 2 * margin, 5);
            }
        }
        
        // Draw blocks if not animating
        if (!animating) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (grid[i][j] > 0) {
                        drawBlock(j, i, grid[i][j]);
                    }
                }
            }
        }
    }

    private void drawBlock(float x, float y, int value) {
        // Determine the color based on the value
        int colorIndex = (int)(Math.log(value) / Math.log(2)) - 1;
        if (colorIndex >= colors.length) {
            colorIndex = colors.length - 1;
        }
        
        fill(colors[colorIndex]);
        rect(x * cellSize + margin, y * cellSize + margin + 100, 
             cellSize - 2 * margin, cellSize - 2 * margin, 5);
        
        // Draw the value
        fill(value <= 4 ? color(119, 110, 101) : color(249, 246, 242));
        textAlign(CENTER, CENTER);
        
        // Adjust font size based on the number of digits
        int digits = (int) Math.log10(value) + 1;
        textSize(cellSize / 2 - (digits - 1) * 5);
        
        text(value, x * cellSize + cellSize / 2, y * cellSize + cellSize / 2 + 100);
    }

    private void animateBlocks() {
        // Draw the base grid without blocks first
        boolean allDone = true;
        
        // Update and draw each animation
        for (int i = 0; i < animations.size(); i++) {
            Animation anim = animations.get(i);
            anim.update();
            
            if (!anim.isComplete()) {
                allDone = false;
            }
            
            float x = anim.getCurrentX();
            float y = anim.getCurrentY();
            drawBlock(x, y, anim.value);
        }
        
        // If all animations are complete, update the grid and check game state
        if (allDone) {
            animations.clear();
            animating = false;
            
            // Add a new random block if a move was performed
            if (movePerformed) {
                addRandomBlock();
                movePerformed = false;
            }
            
            // Check if game is over
            if (isGameOver()) {
                gameOver = true;
                endTime = System.currentTimeMillis();
            }
        }
    }

    private void drawGameOver() {
        // Semi-transparent overlay
        fill(255, 255, 255, 200);
        rect(0, 0, width, height);
        
        // "GAME OVER" text
        textAlign(CENTER, CENTER);
        fill(119, 110, 101);
        textSize(60);
        text("GAME OVER", width / 2, height / 2);
        
        textSize(24);
        text("Press 'r' to restart", width / 2, height / 2 + 50);
    }

    @Override
    public void keyPressed() {
        if (gameOver) {
            if (key == 'r' || key == 'R') {
                restart();
            }
            return;
        }
        
        if (animating) {
            return;
        }
        
        movePerformed = false;
        
        switch (keyCode) {
            case UP:
                movePerformed = moveUp();
                break;
            case DOWN:
                movePerformed = moveDown();
                break;
            case LEFT:
                movePerformed = moveLeft();
                break;
            case RIGHT:
                movePerformed = moveRight();
                break;
            case 'R':
                restart();
                break;
        }
        
        if (movePerformed) {
            animating = true;
        }
    }

    @Override
    public void mousePressed() {
        if (gameOver || animating) {
            return;
        }
        
        // Convert mouse coordinates to grid position
        int gridX = (mouseX) / cellSize;
        int gridY = (mouseY - 100) / cellSize;
        
        // Check if click is within grid boundaries
        if (gridX >= 0 && gridX < gridSize && gridY >= 0 && gridY < gridSize) {
            // Check if the cell is empty
            if (grid[gridY][gridX] == 0) {
                // Add a block at the clicked position (50% chance for 2 or 4)
                int value = random.nextBoolean() ? 2 : 4;
                grid[gridY][gridX] = value;
                
                // Check if game is over after adding the block
                if (isGameOver()) {
                    gameOver = true;
                    endTime = System.currentTimeMillis();
                }
            }
        }
    }

    private boolean moveUp() {
        boolean moved = false;
        int[][] newGrid = new int[gridSize][gridSize];
        
        for (int col = 0; col < gridSize; col++) {
            int[] column = new int[gridSize];
            for (int row = 0; row < gridSize; row++) {
                column[row] = grid[row][col];
            }
            
            int[] mergedColumn = mergeArray(column);
            
            for (int row = 0; row < gridSize; row++) {
                if (newGrid[row][col] != column[row]) {
                    moved = true;
                }
                
                // Create animations
                if (column[row] != 0 && column[row] != mergedColumn[row]) {
                    // Find where this value moved to
                    for (int newRow = 0; newRow < gridSize; newRow++) {
                        if (mergedColumn[newRow] == column[row]) {
                            // Check if this is a merge
                            boolean isMerge = false;
                            int mergeValue = column[row];
                            for (int checkRow = newRow + 1; checkRow < gridSize; checkRow++) {
                                if (mergedColumn[checkRow] == column[row]) {
                                    isMerge = true;
                                    mergeValue = column[row] * 2;
                                    break;
                                }
                            }
                            
                            animations.add(new Animation(col, row, col, newRow, column[row], isMerge, mergeValue));
                            break;
                        }
                    }
                }
                
                newGrid[row][col] = mergedColumn[row];
            }
        }
        
        if (moved) {
            grid = newGrid;
        }
        
        return moved;
    }

    private boolean moveDown() {
        boolean moved = false;
        int[][] newGrid = new int[gridSize][gridSize];
        
        for (int col = 0; col < gridSize; col++) {
            int[] column = new int[gridSize];
            for (int row = 0; row < gridSize; row++) {
                column[gridSize - 1 - row] = grid[row][col];
            }
            
            int[] mergedColumn = mergeArray(column);
            
            for (int row = 0; row < gridSize; row++) {
                int newRow = gridSize - 1 - row;
                if (newGrid[row][col] != column[newRow]) {
                    moved = true;
                }
                
                // Create animations
                if (column[newRow] != 0 && column[newRow] != mergedColumn[newRow]) {
                    // Find where this value moved to
                    for (int mRow = 0; mRow < gridSize; mRow++) {
                        if (mergedColumn[mRow] == column[newRow]) {
                            // Check if this is a merge
                            boolean isMerge = false;
                            int mergeValue = column[newRow];
                            for (int checkRow = mRow + 1; checkRow < gridSize; checkRow++) {
                                if (mergedColumn[checkRow] == column[newRow]) {
                                    isMerge = true;
                                    mergeValue = column[newRow] * 2;
                                    break;
                                }
                            }
                            
                            animations.add(new Animation(col, gridSize - 1 - newRow, col, gridSize - 1 - mRow, column[newRow], isMerge, mergeValue));
                            break;
                        }
                    }
                }
                
                newGrid[row][col] = mergedColumn[newRow];
            }
        }
        
        if (moved) {
            grid = newGrid;
        }
        
        return moved;
    }

    private boolean moveLeft() {
        boolean moved = false;
        int[][] newGrid = new int[gridSize][gridSize];
        
        for (int row = 0; row < gridSize; row++) {
            int[] rowArray = new int[gridSize];
            for (int col = 0; col < gridSize; col++) {
                rowArray[col] = grid[row][col];
            }
            
            int[] mergedRow = mergeArray(rowArray);
            
            for (int col = 0; col < gridSize; col++) {
                if (newGrid[row][col] != rowArray[col]) {
                    moved = true;
                }
                
                // Create animations
                if (rowArray[col] != 0 && rowArray[col] != mergedRow[col]) {
                    // Find where this value moved to
                    for (int newCol = 0; newCol < gridSize; newCol++) {
                        if (mergedRow[newCol] == rowArray[col]) {
                            // Check if this is a merge
                            boolean isMerge = false;
                            int mergeValue = rowArray[col];
                            for (int checkCol = newCol + 1; checkCol < gridSize; checkCol++) {
                                if (mergedRow[checkCol] == rowArray[col]) {
                                    isMerge = true;
                                    mergeValue = rowArray[col] * 2;
                                    break;
                                }
                            }
                            
                            animations.add(new Animation(col, row, newCol, row, rowArray[col], isMerge, mergeValue));
                            break;
                        }
                    }
                }
                
                newGrid[row][col] = mergedRow[col];
            }
        }
        
        if (moved) {
            grid = newGrid;
        }
        
        return moved;
    }

    private boolean moveRight() {
        boolean moved = false;
        int[][] newGrid = new int[gridSize][gridSize];
        
        for (int row = 0; row < gridSize; row++) {
            int[] rowArray = new int[gridSize];
            for (int col = 0; col < gridSize; col++) {
                rowArray[gridSize - 1 - col] = grid[row][col];
            }
            
            int[] mergedRow = mergeArray(rowArray);
            
            for (int col = 0; col < gridSize; col++) {
                int newCol = gridSize - 1 - col;
                if (newGrid[row][col] != rowArray[newCol]) {
                    moved = true;
                }
                
                // Create animations
                if (rowArray[newCol] != 0 && rowArray[newCol] != mergedRow[newCol]) {
                    // Find where this value moved to
                    for (int mCol = 0; mCol < gridSize; mCol++) {
                        if (mergedRow[mCol] == rowArray[newCol]) {
                            // Check if this is a merge
                            boolean isMerge = false;
                            int mergeValue = rowArray[newCol];
                            for (int checkCol = mCol + 1; checkCol < gridSize; checkCol++) {
                                if (mergedRow[checkCol] == rowArray[newCol]) {
                                    isMerge = true;
                                    mergeValue = rowArray[newCol] * 2;
                                    break;
                                }
                            }
                            
                            animations.add(new Animation(gridSize - 1 - newCol, row, gridSize - 1 - mCol, row, rowArray[newCol], isMerge, mergeValue));
                            break;
                        }
                    }
                }
                
                newGrid[row][col] = mergedRow[newCol];
            }
        }
        
        if (moved) {
            grid = newGrid;
        }
        
        return moved;
    }

    // Helper method to merge an array in the 2048 game logic
    private int[] mergeArray(int[] array) {
        int[] result = new int[array.length];
        int index = 0;
        
        // Move all non-zero elements to the front
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0) {
                result[index++] = array[i];
            }
        }
        
        // Merge adjacent elements if they are the same
        for (int i = 0; i < index - 1; i++) {
            if (result[i] == result[i + 1]) {
                result[i] *= 2;
                result[i + 1] = 0;
            }
        }
        
        // Compact the array again to remove zeros created by merging
        int[] compacted = new int[array.length];
        index = 0;
        for (int i = 0; i < array.length; i++) {
            if (result[i] != 0) {
                compacted[index++] = result[i];
            }
        }
        
        return compacted;
    }

    private void addRandomBlock() {
        ArrayList<Integer> emptyX = new ArrayList<>();
        ArrayList<Integer> emptyY = new ArrayList<>();
        
        // Find all empty cells
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] == 0) {
                    emptyX.add(j);
                    emptyY.add(i);
                }
            }
        }
        
        if (!emptyX.isEmpty()) {
            // Choose a random empty cell
            int index = random.nextInt(emptyX.size());
            int x = emptyX.get(index);
            int y = emptyY.get(index);
            
            // Randomly choose 2 or 4 with equal probability
            grid[y][x] = random.nextBoolean() ? 2 : 4;
        }
    }

    private boolean isGameOver() {
        // Check if there are any empty cells
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] == 0) {
                    return false;
                }
            }
        }
        
        // Check if there are any adjacent cells with the same value
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int current = grid[i][j];
                
                // Check right neighbor
                if (j < gridSize - 1 && grid[i][j + 1] == current) {
                    return false;
                }
                
                // Check bottom neighbor
                if (i < gridSize - 1 && grid[i + 1][j] == current) {
                    return false;
                }
            }
        }
        
        // No moves possible
        return true;
    }

    private void restart() {
        // Reset the grid
        grid = new int[gridSize][gridSize];
        
        // Reset game state
        gameOver = false;
        startTime = System.currentTimeMillis();
        animations.clear();
        animating = false;
        
        // Add two initial blocks
        addRandomBlock();
        addRandomBlock();
    }
}