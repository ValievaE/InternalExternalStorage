package com.example.internalexternalstorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLogin;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonRegistration;
    private CheckBox checkBox;
    private File file;
    public static final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 100;
    private final static String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    public static final String APP_PREFERENCES = "state";
    public static final String APP_PREFERENCES_CHECKBOX = "checkbox_state";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor myEditor = sharedPreferences.edit();
        myEditor.putBoolean(APP_PREFERENCES_CHECKBOX, checkBox.isChecked());
        myEditor.apply();

        getDateFromSharedPref();


        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Разрешена запись в файл", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_WRITE_STORAGE);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (!checkBox.isChecked()) {
                    Toast.makeText(MainActivity.this, "Запись во внутреннее хранилище", Toast.LENGTH_SHORT).show();

                    buttonRegistration.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            writeLoginPass(getFilesDir());
                        }
                    });

                    buttonLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String loginPass = readLoginPass(getFilesDir());
                            String input = editTextLogin.getText().toString() + editTextPassword.getText().toString();
                            if (loginPass.equals(input)) {
                                Toast.makeText(MainActivity.this, "Верно", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Логин или пароль введен неверно", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                } else {
                    Toast.makeText(MainActivity.this, "Запись во внешнее хранилище", Toast.LENGTH_SHORT).show();

                    buttonRegistration.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isExternalStorageWritable()) {
                                writeLoginPass(getExternalFilesDir(null));
                            }
                        }
                    });

                    buttonLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isExternalStorageWritable()) {
                                String loginPass = readLoginPass(getExternalFilesDir(null));
                                String input = editTextLogin.getText().toString() + editTextPassword.getText().toString();
                                if (loginPass.equals(input)) {
                                    Toast.makeText(MainActivity.this, "Верно", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Логин или пароль введен неверно", Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                    });

                }

            }
        });

    }

    private void getDateFromSharedPref() {
        boolean b = sharedPreferences.getBoolean(APP_PREFERENCES_CHECKBOX, false);
        checkBox.setChecked(b);
    }


    private void writeLoginPass(File directory) {
        file = new File(directory, "file_login_pass.txt");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput("file_login_pass.txt", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bw = new BufferedWriter(outputStreamWriter);

        try {
            bw.write(editTextLogin.getText().toString());
            bw.write(editTextPassword.getText().toString());
            if (editTextLogin.getText().toString().isEmpty() || editTextPassword.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Введите логин или пароль", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Данные сохранены", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private String readLoginPass(File directory) {
        String line = "";

        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStreamReader = new InputStreamReader(openFileInput("file_login_pass.txt"));
            bufferedReader = new BufferedReader(inputStreamReader);
            line = bufferedReader.readLine();
            StringBuilder lines = new StringBuilder();

            while (line != null) {
                Log.d(TAG, "line");
                lines = lines.append(line);
                line = bufferedReader.readLine();
            }
            line = lines.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }


    private void init() {
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLodin);
        buttonRegistration = findViewById(R.id.buttonRegistration);
        checkBox = findViewById(R.id.checkBox);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Разрешение на доступ к внешнему хранилищу", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Вы не получили разрешение на доступ к внешнему хранилищу", Toast.LENGTH_LONG).show();
                    finish();
                }

        }
    }
}