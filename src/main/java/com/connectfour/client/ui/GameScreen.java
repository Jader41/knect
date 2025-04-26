public GameScreen(Stage stage, GameClient gameClient) {
    this.stage = stage;
    this.gameClient = gameClient;
    this.isAIGame = false;
    
    // Register listeners
    gameClient.addGameStateListener(this);
    gameClient.addConnectionListener(this);
    gameClient.addChatMessageListener(this);
    
    // Get initial game state
    this.gameState = gameClient.getCurrentGameState();
    this.playerColor = gameClient.getAssignedColor();
    this.opponentUsername = gameClient.getOpponentUsername();
    
    // Initialize background music
    this.backgroundMusic = new AudioPlayer("somber_background.mp3");
}

/**
 * Constructor for AI game.
 */
public GameScreen(Stage stage, GameClient gameClient, GameState gameState, PlayerColor playerColor, String aiDifficulty) {
    this.stage = stage;
    this.gameClient = gameClient;
    this.gameState = gameState;
    this.playerColor = playerColor;
    this.opponentUsername = "Computer (" + aiDifficulty + ")";
    this.isAIGame = true;
    this.aiDifficulty = aiDifficulty;
    
    // Create AI player
    PlayerColor aiColor = (playerColor == PlayerColor.RED) ? PlayerColor.YELLOW : PlayerColor.RED;
    this.aiPlayer = new AIPlayer(aiDifficulty, aiColor);
    
    // Determine if AI goes first (if AI is RED)
    this.isAITurn = (aiColor == PlayerColor.RED);
    
    // Initialize background music
    this.backgroundMusic = new AudioPlayer("somber_background.mp3");
}

/**
 * Constructor for AI game with username and difficulty.
 */
public GameScreen(Stage stage, GameClient gameClient, String username, String opponentLabel, String aiDifficulty) {
    this.stage = stage;
    this.gameClient = gameClient;
    this.isAIGame = true;
    this.aiDifficulty = aiDifficulty; // Use provided difficulty
    this.opponentUsername = opponentLabel;
    
    // Create a new local game state
    this.gameState = new GameState(username, "Computer");
    
    // Player always starts with RED in local games
    this.playerColor = PlayerColor.RED;
    
    // Create AI player with YELLOW
    this.aiPlayer = new AIPlayer(aiDifficulty, PlayerColor.YELLOW);
    this.isAITurn = false; // Player goes first
    
    // Initialize background music
    this.backgroundMusic = new AudioPlayer("somber_background.mp3");
} 