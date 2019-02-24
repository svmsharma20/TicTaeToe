/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serverPrgms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Shivam
 */
public class TicTaeToeClient extends Application implements TicTaeToeConstants
{
    private boolean myTurn = false, continueToPlay = true, waiting = true, restart=false;
    private char myToken = ' ' , otherToken= ' ';
    
 //   private Cell[][] cell = new Cell[3][3];
    private CellBtn[][] cellBtns = new CellBtn[3][3];
    
    private Label titleLbl = new Label();
    private Label statusLbl = new Label();
    
    private Button restartBtn = new Button("Restart");
    private Button quitBtn = new Button("Quit");
    
    private int rowSelected,colSelected;
    
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    
    private String host = "localhost";
    
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        GridPane pane = new GridPane();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
            { 
                cellBtns[i][j] = new CellBtn(i, j);
                cellBtns[i][j].setStyle("-fx-color : lightblue ");
                
                pane.add(cellBtns[i][j],j,i);            
            }
        restartBtn.setMinSize(205, 35);
        quitBtn.setMinSize(205, 35);
        
        restartBtn.setDisable(true);
        quitBtn.setDisable(true);
//        BorderPane borderPane = new BorderPane();
//        borderPane.setTop(titleLbl);
//        borderPane.setCenter(pane);
//        borderPane.setBottom(statusLbl);
        HBox hBox = new HBox(restartBtn,quitBtn);
        VBox borderPane = new VBox(titleLbl,pane,statusLbl,hBox);
        
        Scene myScene = new Scene(borderPane, 400, 480);
        primaryStage.setTitle("Tic-Tae-Toe");
        primaryStage.setScene(myScene);
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setResizable(false);
        
        quitBtn.setOnAction(e ->
        {
            try
            {
                toServer.writeInt(QUIT);
            } catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
            System.exit(0);
        });
        
        restartBtn.setOnAction(e ->
        {
            try
            {
                toServer.writeInt(RESTART);
            } catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
        });
        myServerConnection();
    }   
    
    public void reset()
    {
        continueToPlay = true;
        
    }
    public void myServerConnection()
    {
        try{
            Socket socket = new  Socket(host, 8000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
            
            new Thread(new GameStarter()).start();
        }catch(IOException e)
        {
            System.out.println(e.toString());
        }
    }
    
    public class GameStarter implements Runnable
    {

        @Override
        public void run()
        {
            try{
                int player = fromServer.readInt();
                
                if (player == PLAYER1)
                {
                    myToken = 'X';
                    otherToken = '0';
                    Platform.runLater(() ->
                    {
                        titleLbl.setText("Player 1 with token X");
                        statusLbl.setText("Waiting for Player 2 to join");
                    });
                    
                    fromServer.readInt();
                    
                    Platform.runLater(() ->
                    {
                        statusLbl.setText("Player 2 to joined");
                    });
                    
                    myTurn = true;
                }
                else if(player ==  PLAYER2)
                {
                    myToken = '0';
                    otherToken = 'X';
                    Platform.runLater(() ->
                    {
                        titleLbl.setText("Player 2 with token 0");
                        statusLbl.setText("Waiting for Player 1 's move");
                    });                   
                }
                
                while (continueToPlay)
                {
                    if(player == PLAYER1)                    
                    {
                        waitForMove();
                        sendMove();
                        receiveInfoFromServer();
                    }
                    else if(player == PLAYER2)
                    {
                        receiveInfoFromServer();
                        waitForMove();
                        sendMove();
                    }
                }
            }catch(IOException e)
            {
                System.out.println(e.toString());
            } catch (InterruptedException e)
            {
                System.out.println(e.toString());
            }
        }
        
        public void waitForMove() throws InterruptedException
        {
            while (waiting)
                Thread.sleep(50);
            
            waiting = true;
        }
        
        public void sendMove() throws IOException
        {
            
            toServer.writeInt(rowSelected);
            System.out.println("line 158");
            toServer.writeInt(colSelected);
            System.out.println("line 161");
        }
        
        public void receiveInfoFromServer() throws IOException
        {
            int status = fromServer.readInt();
            
            if(status ==  PLAYER1_WON)
            {
                continueToPlay = false;
                if(myToken=='X')
                { Platform.runLater(() -> statusLbl.setText("I Won! (X)"));
                  Platform.runLater(() -> titleLbl.setText("Press R : Restart, Q : Quit"));  
                  restartBtn.setDisable(false);
                  quitBtn.setDisable(false);
                }
                else
                {   Platform.runLater(() -> statusLbl.setText("Player 1 (X) Won!"));
                    Platform.runLater(() -> titleLbl.setText("Press R : Restart, Q : Quit"));
                    restartBtn.setDisable(false);
                    quitBtn.setDisable(false);
                }
                
                receiveMove();
            }
            else if(status == PLAYER2_WON)
            {
                continueToPlay = false;
                if(myToken=='X')
                {    Platform.runLater(() -> statusLbl.setText("Player 2 (0) Won!"));
                    Platform.runLater(() -> titleLbl.setText("Press R : Restart, Q : Quit"));
                    restartBtn.setDisable(false);
                    quitBtn.setDisable(false);
                }
                else
                {    Platform.runLater(() -> statusLbl.setText("I Won! (0)"));
                    Platform.runLater(() -> titleLbl.setText("Press R : Restart, Q : Quit"));
                    restartBtn.setDisable(false);
                    quitBtn.setDisable(false);
                }
                
                receiveMove();
            }
            else if(status == DRAW)
            {
                continueToPlay = false;
                Platform.runLater(() -> statusLbl.setText("Game Draw..."));
                Platform.runLater(() -> titleLbl.setText("Press R : Restart, Q : Quit"));
                restartBtn.setDisable(false);
                  quitBtn.setDisable(false);
                if(myToken == '0')
                    receiveMove();
            }
            else
            {
                receiveMove();
                Platform.runLater(() -> statusLbl.setText("My turn...."));
                myTurn = true;
            }
        }
        
        public void receiveMove() throws IOException
        {
            int row  = fromServer.readInt();
            System.out.println("209 : "+row);
            int col = fromServer.readInt();
            System.out.println("211 : "+col);
            Platform.runLater(() -> cellBtns[row][col].setTocken(otherToken));
        }
    }
    
     public class CellBtn extends Button
    {
        private int row;
        private int col;
        
        private char tocken=' '; 

        public CellBtn(int row, int col)
        {
            this.row = row;
            this.col = col;
            setPrefSize(2000, 2000);
            setFont(new Font(64));
            //setStyle("-fx-border-color : black");
            setOnMouseClicked(e -> handleMouseClick());
            setOnAction(e -> handleMouseClick());            
            setOnKeyReleased(e -> {
                if(e.getText().equalsIgnoreCase("Q"))
                    System.exit(0);
                
            });
        }
        
        public char getTocken()
        {
            return tocken;
        }
        
        public void setTocken(char t)
        {
            tocken = t;
            repaint();
        }
        protected void repaint()
        {
            if(tocken == 'X')
               setText("X");
            else if(tocken == '0')
               setText("0");
            
            setDisabled(true);
        }
        private void handleMouseClick()
        {
            if(tocken==' ' && myTurn)
            {
                setTocken(myToken);
                myTurn=false;
                rowSelected=this.row;
                colSelected=this.col;
                statusLbl.setText("\nWaiting for other player to move....");
                waiting=false;
            }
        }
}
     
// public class Cell extends Pane
//    {
//        private int row;
//        private int col;
//        
//        private char tocken=' ';
//
//        public Cell(int row, int col)
//        {
//            this.row = row;
//            this.col = col;
//            setPrefSize(2000, 2000);
//            setStyle("-fx-border-color : black");
//            setOnMouseClicked(e -> handleMouseClick());
//        }
//        
//        public char getTocken()
//        {
//            return tocken;
//        }
//        
//        public void setTocken(char t)
//        {
//            tocken = t;
//            repaint();
//        }
//        protected void repaint()
//        {
//            
//        }
//        private void handleMouseClick()
//        {
//            
//        }
//    }
        
}
