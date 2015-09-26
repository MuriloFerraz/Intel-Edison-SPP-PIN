package com.maker.muriloferraz.spppinedison;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import com.maker.muriloferraz.spppinedison.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LEDOnOff";

    Button btnOn, btnOff;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = "00:00:00:00:00:00"; // coloque o MAC da sua placa
    // para saber o endereco mac da Intel Edison
    // no serial terminal digite:
    // # hcitool dev
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In onCREATE()");
        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        btnOn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("1");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Voce clicou em: Ligar", Toast.LENGTH_SHORT);
                msg.show();
            }
        });

        btnOff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("0");
                Toast msg = Toast.makeText(getBaseContext(),
                        "Voce clicou em: Desligar", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "... em onResume() - Tentando conectar com o dispositivo ...");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

        }
        catch (IOException e)
        {
            errorExit("Erro fatal", "em onResume() na criacao do socket: " + e.getMessage()+ ".");
        }

        btAdapter.cancelDiscovery();
        Log.d(TAG, "... Conectando com o dispositivo");

        try
        {
           btSocket.connect();
           Log.d(TAG, "Conexao estabelecida, dados sendo transmitidos ...");
        }
        catch (IOException e)
        {
            try
            {
             btSocket.close();
            }
            catch (IOException e2)
            {
                errorExit("Erro Fatal", " Em onResume() ao fechar a conexao durante falha " + e2.getMessage()+"." );
            }
        }

        Log.d(TAG, "...criando conexao Socket...");

        try
        {
            outStream = btSocket.getOutputStream();
        }
        catch (IOException e)
        {
            errorExit("Erro fatal", "em onResume() ao prepara os dados a serem enviados: "+ e.getMessage()+ ".");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(TAG, "Em onPause() ...");

        if(outStream != null)
        {
            try
            {
                outStream.flush();
            }
            catch (IOException e)
            {
                errorExit("Erro Fatal","Em onPause() no fluxo de saida dos dados "+ e.getMessage()+"." );
            }
        }

        try
        {
          btSocket.close();
        }
        catch (IOException e2)
        {
            errorExit("Erro Fatal", "Em onPause() ao fechar o socket"+e2.getMessage()+".");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkBTState()
    {
        if(btAdapter==null)
        {
            errorExit("Erro Fatal", "Bluetooth nao suportado. Abortando");
        }
        else
        {
            if(btAdapter.isEnabled())
            {
                Log.d(TAG,"...Bluetooth esta ativado");
            }
            else
            {
                Intent enableBTIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message)
    {
        Toast msg = Toast.makeText(getBaseContext(),title + "-" + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void sendData(String message)
    {
        byte[] msgBuffer = message.getBytes();
        Log.d(TAG, "Enviando Dados: " + message +" ...");

        try
        {
            outStream.write(msgBuffer);

        }
        catch (IOException e)
        {
            String msg = "In OnResume() Durante envio de dados Ocorreu a excessao: "+ e.getMessage();
            if(address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\n altere a linha 36 do arduivo .java onde contem 00:00:00:00:00:00";
                msg = msg + ".\n\n verifique se SPP UUID:" + MY_UUID.toString() + "existe.\n\n";

                errorExit("Erro Fatal", msg);
        }
    }

}

