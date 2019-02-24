/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serverPrgms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import static serverPrgms.TicTaeToeConstants.CONTINUE;
import static serverPrgms.TicTaeToeConstants.DRAW;
import static serverPrgms.TicTaeToeConstants.PLAYER1;
import static serverPrgms.TicTaeToeConstants.PLAYER1_WON;
import static serverPrgms.TicTaeToeConstants.PLAYER2;
import static serverPrgms.TicTaeToeConstants.PLAYER2_WON;


/**
 *
 * @author Shivam
 */
public class TicTaeToeServer extends Application implements TicTaeToeConstants
{
    TextArea ta = new TextArea();
    int sessionNo = 1;
    
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Scene myScene = new Scene(new ScrollPane(ta), 450, 200);
        primaryStage.setTitle("Tic-Tae-Toe Server..");
        primaryStage.setScene(myScene);
        primaryStage.show();
        
        ta.setEditable(false);
        
        new Thread(new PlayerInitiator()).start();
    }   
    
    class PlayerInitiator implements Runnable
    {

        @Override
        public void run()
        {
            try{
                
                ServerSocket serverSocket = new ServerSocket(8000);
                
                Platform.runLater(()->
                {
                    ta.appendText(new Date() + " : Server Started..");
                });
                
                while (true)
                {
                    Platform.runLater(()->
                    {
                        ta.appendText("\n\nSession"+sessionNo+" is Initiated.....");
                        ta.appendText("\nWaiting for Player 1 to connect.....");
                    });
                    
                    Socket p1Socket = serverSocket.accept();
                    
                    new DataOutputStream(p1Socket.getOutputStream()).writeInt(PLAYER1);
                    
                    Platform.runLater(()->
                    {
                        ta.appendText("\nPlayer 1 joined session"+sessionNo);
                        ta.appendText("\nPlayer 1 IP address : "+p1Socket.getInetAddress().getHostAddress());
                        ta.appendText("\nWaiting for Player 2 to connect..");
                    });
                    
                    Socket p2Socket = serverSocket.accept();
                    
                    new DataOutputStream(p2Socket.getOutputStream()).writeInt(PLAYER2);
                    
                    Platform.runLater(()->
                    {
                        ta.appendText("\nPlayer 2 joined session"+sessionNo);
                        ta.appendText("\nPlayer 2 IP address : "+p2Socket.getInetAddress().getHostAddress());
                        ta.appendText("\nStarting the game...............................");
                        ta.appendText("\nPlz wait...............................");
                    });
                    
                    new Thread(new HandleASession(p1Socket,p2Socket)).start();
                    sessionNo++;
                }
                
            } catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
                
        }       
    }
    
    class HandleASession implements Runnable
    {
        private Socket p1,p2;
        private char cell[][] = new char[3][3];
        
        private DataInputStream fromPlayer1,fromPlayer2;
        private DataOutputStream toPlayer1,toPlayer2;
        
        public HandleASession(Socket p1, Socket p2)
        {
            this.p1 = p1;
            this.p2 = p2;
            
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    cell[i][j]=' ';
                }
            }
        }
               
        @Override
        public void run()
        {
            try
            {
                fromPlayer1 = new DataInputStream(p1.getInputStream());
                fromPlayer2 = new DataInputStream(p2.getInputStream());
                toPlayer1   = new DataOutputStream(p1.getOutputStream());
                toPlayer2   = new DataOutputStream(p2.getOutputStream());
                
                toPlayer1.writeInt(1);
               // toPlayer2.writeInt(2);
                
                while (true)
                {
                    int row = fromPlayer1.readInt();
                    System.out.println("line 150  row : "+row);
                    int col = fromPlayer1.readInt();
                    System.out.println("line 152 col : "+col);
                    cell[row][col] = 'X';
                    
                    
                    System.out.println("line 156");
                    
                    if(isWon('X'))
                    {
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, col);
                        break;
                    }
                    else if(isFull())
                    {
                        toPlayer1.writeInt(DRAW);
                        toPlayer2.writeInt(DRAW);
                        sendMove(toPlayer2, row, col);
                        break;
                    }
                    else
                    {   toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, row, col);
                    }    
                    
                    System.out.println("line 173");
                    row = fromPlayer2.readInt();
                    System.out.println("line 175");
                    col = fromPlayer2.readInt();
                    System.out.println("line 177");
                    cell[row][col] = '0';
                    
                    
                    System.out.println("line 181");
                    if(isWon('0'))
                    {
                        toPlayer1.writeInt(PLAYER2_WON);
                        System.out.println("line 185");
                        toPlayer2.writeInt(PLAYER2_WON);
                        System.out.println("line 187");
                        sendMove(toPlayer1, row, col);
                        
                        break;
                    }
                    else
                    {   toPlayer1.writeInt(CONTINUE);
                        sendMove(toPlayer1, row, col);
                    }
                    
                }
                
            } catch (IOException ex)
            {
                System.out.println(ex.toString());
            }
        }
        
        private void sendMove(DataOutputStream out, int row, int col) throws IOException
        {
            out.writeInt(row);
            out.writeInt(col);
        }
        
        private boolean isWon(char tocken)
        {
            for (int i = 0; i < 3; i++)
            {
                if( cell[i][0] == tocken && cell[i][1] == tocken && cell[i][2] == tocken)               //check all rows
                    return true;
                else if(cell[0][i] == tocken && cell[1][i] == tocken && cell[2][i] == tocken)           //check all columns
                    return true;
            }   
            
            if(cell[0][0] == tocken && cell[1][1] == tocken && cell[2][2] == tocken)           //check major diagonal
                    return true;
                else if(cell[0][2] == tocken && cell[1][1] == tocken && cell[2][0] == tocken)  //check minor diagonal
                    return true;
            
            
            return false;
        }
        
        private boolean isFull()
        {
            for (int i = 0; i < 3; i++)
              for (int j = 0; j < 3; j++)
                  if(cell[i][j] == ' ')
                    return false;
                
            return true;
        }
    }
    
    class Restarter implements Runnable
    {
        Socket player;

        public Restarter(Socket player)
        {
            this.player = player;
        }
        
        public void run()
        {
            try
            {
                int status = new DataInputStream(player.getInputStream()).readInt();
                if(status == RESTART)
                {
                    
                }
                else if(status == QUIT)
                {
                    
                }
                    
            } catch (IOException ex)
            {
                Logger.getLogger(TicTaeToeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
