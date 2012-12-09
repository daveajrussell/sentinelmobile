package com.sentinel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONStringer;

public class SentinelLogin extends Activity {

    private static final int LOGIN_REQUEST_CODE;

    static  {
        LOGIN_REQUEST_CODE = 1;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button btnLogin = (Button)findViewById(R.id.btn_login);
        final EditText txtUsername = (EditText)findViewById(R.id.txt_username);
        final EditText txtPassword = (EditText)findViewById(R.id.txt_password);
        final View pgLoginProgress = (View)findViewById(R.id.pb_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent sentinelIntent = new Intent(SentinelLogin.this, Sentinel.class);
                //startActivity(sentinelIntent);

                //pgLoginProgress.setVisibility(View.VISIBLE);
                //txtUsername.setEnabled(false);
                //txtPassword.setEnabled(false);



            }
        });
    }

    public String convertCredentialsObjectToJSONString(Credentials oCredentials) {

        Gson oGson = new Gson();
        JSONStringer oCredentialsStringer;
        String strCredentialsJSON = null;

        try {
            oCredentialsStringer = new JSONStringer()
                    .object()
                        .key("").value(oCredentials.getUsername())
                        .key("").value(oCredentials.getPassword())
                    .endObject();

            strCredentialsJSON = oGson.toJson(oCredentialsStringer.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return strCredentialsJSON;
    }
}