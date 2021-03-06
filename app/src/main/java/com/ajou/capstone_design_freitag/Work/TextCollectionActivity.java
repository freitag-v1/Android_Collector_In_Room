package com.ajou.capstone_design_freitag.Work;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ajou.capstone_design_freitag.API.RESTAPI;
import com.ajou.capstone_design_freitag.PopupActivity;
import com.ajou.capstone_design_freitag.R;
import com.ajou.capstone_design_freitag.UI.dto.Project;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TextCollectionActivity extends AppCompatActivity {

    private static final int COLLECTION_TEXT_REQUEST_CODE = 100;
    private static final int POP_UP_REQUEST_CODE = 101;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    AppCompatDialog progressDialog;

    //view
    private TextView projectName;
    private TextView wayContent;
    private TextView conditionContent;
    private static TextView exampleContent;
    private TextView requester;
    private TextView classListView;
    private TextView dataURI;
    private RadioGroup selectClass;
    private Button select;
    private Button add;
    private Button upload;
    private Context context;
    private Project project;
    private String classname;

    private List<InputStream> inputStreamList = new ArrayList<>();
    private List<EditText> questionList = new ArrayList<>();
    private List<EditText> answerList = new ArrayList<>();
    private List<String> fileNameList = new ArrayList<>();
    private List<String> classList = new ArrayList<>();
    private boolean classCheck = false;

    private LinearLayout selectFile;
    private LinearLayout selectUser;
    private LinearLayout setLayout;
    private RadioButton fileUpload;
    private RadioButton fileUser;
    int editTextNum=0;
    private Uri exampleDataUri;

    static File file = new File("/data/data/com.ajou.capstone_design_freitag/files/project_example.txt");
    private OutputStream outputStream = new FileOutputStream(file);

    public TextCollectionActivity() throws FileNotFoundException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_collection);
        context = getApplicationContext();

        Intent intent = getIntent();
        project = intent.getParcelableExtra("project"); //리스트에서 사용자가 선택한 프로젝트 정보 받아옴
        Project.getProjectinstance().setBucketName(project.getBucketName());
        Project.getProjectinstance().setProjectId(project.getProjectId());

        findViews();
    }

    private void findViews() {
        projectName = findViewById(R.id.text_collection_project_name);
        wayContent = findViewById(R.id.text_collection_way_content);
        conditionContent = findViewById(R.id.text_collection_condition_content);
        dataURI = findViewById(R.id.collection_text_uri);
        select = findViewById(R.id.collection_text_select); //파일선택
        add = findViewById(R.id.collection_text_user); //직접 입력
        upload = findViewById(R.id.collection_upload_text);
        exampleContent = findViewById(R.id.work_example_content_text);
        selectClass = findViewById(R.id.radioGroup_class_list_text);

        requester = findViewById(R.id.text_collection_work_requester);
        classListView = findViewById(R.id.text_classlist_project_detail);

        setLayout = findViewById(R.id.question_and_answer);//질문&답 세트 넣을 레이아웃
        selectFile = findViewById(R.id.select_file);//파일선택 레이아웃
        selectUser = findViewById(R.id.select_user);//직접입력 레이아웃

        selectFile.setVisibility(View.GONE);
        selectUser.setVisibility(View.GONE);

        fileUpload = findViewById(R.id.text_file_upload);
        fileUser = findViewById(R.id.user_input);

        for(int i=0;i<project.getClass_list().size();i++){
            RadioButton radioButton = new RadioButton(context);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            radioButton.setLayoutParams(param);
            radioButton.setText(project.getClass_list().get(i));
            radioButton.setId(i);
            selectClass.addView(radioButton);
        }

        getExampleData();

        projectName.setText(project.getProjectName());
        wayContent.setText(project.getWayContent());
        conditionContent.setText(project.getConditionContent());

        requester.setText(project.getUserId());
        classListView.setText(project.getClass_list().toString());

        //라디오버튼 리스너
        selectClass.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for(int i=0;i<project.getClass_list().size();i++){
                    if(i == checkedId){
                        classname =  project.getClass_list().get(i);
                    }
                }
            }
        });

        fileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classCheck = false;
                selectFile.setVisibility(View.VISIBLE);
                selectUser.setVisibility(View.GONE);
                setLayout.setVisibility(View.GONE);
            }
        });

        fileUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classCheck = true;
                selectFile.setVisibility(View.GONE);
                selectUser.setVisibility(View.VISIBLE);
                setLayout.setVisibility(View.VISIBLE);
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_collection_text_data(v);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView question = new TextView(context);
                question.setLayoutParams(params);
                question.setText("질문: ");
                setLayout.addView(question);
                editTextNum++;

                EditText questionEditText = new EditText(context);
                questionEditText.setLayoutParams(params);
                questionEditText.setId(editTextNum);
                setLayout.addView(questionEditText);
                questionList.add(questionEditText);
                editTextNum++;

                TextView answer = new TextView(context);
                answer.setLayoutParams(params);
                answer.setText("답: ");
                setLayout.addView(answer);
                editTextNum++;

                EditText answerEditText = new EditText(context);
                answerEditText.setLayoutParams(params);
                answerEditText.setId(editTextNum);
                setLayout.addView(answerEditText);
                answerList.add(answerEditText);
                editTextNum++;

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataURI.setText("");
                if(classCheck) {
                    try {
                        userInputData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (inputStreamList.size()==0){
                    Toast.makeText(context,"데이터를 업로드 하셔야 합니다.",Toast.LENGTH_LONG).show();
                }
                else {
                    upload_text_data(inputStreamList,fileNameList,classname);
                    progressON(TextCollectionActivity.this,"Loading..");
                    Toast.makeText(context, "작업 완료",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void select_collection_text_data(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setDataAndType(MediaStore.Downloads.EXTERNAL_CONTENT_URI, "text/*");
        startActivityForResult(intent, COLLECTION_TEXT_REQUEST_CODE);
    }

    private void getExampleData(){
        DownloadExampleTask downloadExampleTask = new DownloadExampleTask();
        downloadExampleTask.execute(project.getBucketName(),project.getExampleContent(),outputStream);
    }

    private static class DownloadExampleTask extends  AsyncTask<Object, Void, Boolean>{
        protected Boolean doInBackground(Object... dataInfos) {
            Boolean result = RESTAPI.getInstance().downloadObject((String)dataInfos[0],(String)dataInfos[1],(OutputStream)dataInfos[2]);
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                System.out.println("예시 다운로드 실패");
            }
            else
            {
                System.out.println("예시 다운로드 성공");
                InputStream inputStream = null;
                StringBuffer strBuffer = new StringBuffer();
                try {
                    inputStream = new FileInputStream(file);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while((line=reader.readLine())!=null){
                        strBuffer.append(line+"\n");
                    }
                    reader.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONParser jsonParser = new JSONParser();
                String example = "";
                try {
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(strBuffer.toString());
                    JSONArray setArray = (JSONArray) jsonObject.get("set");
                    for(int i=0;i<setArray.size();i++){
                        JSONObject set = (JSONObject)setArray.get(i);
                        example+="질문:"+set.get("question")+"\n";
                        example+="답:"+set.get("answer")+"\n";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //exampleContent.setText(strBuffer.toString());
                exampleContent.setText(example);
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void upload_text_data(List<InputStream> inputStreams, List<String> filenames, String classname){
        UploadTextTask uploadImageTask = new UploadTextTask();
        uploadImageTask.execute(inputStreams,filenames,classname);
    }

    public class UploadTextTask extends AsyncTask<Object, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Object... info) {
            try {

                boolean result = RESTAPI.getInstance().collectionWork((List<InputStream>) info[0], (List<String>) info[1], "text/txt",(String)info[2]);
                return new Boolean(result);
            } catch (Exception e) {
                e.printStackTrace();
                return new Boolean(false);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                progressOFF();
                Toast.makeText(context, "수집 작업 텍스트 업로드 성공", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, PopupActivity.class);
                intent.putExtra("msg","작업을 계속 하시겠습니까?");
                startActivityForResult(intent, POP_UP_REQUEST_CODE);
            } else {
                progressOFF();
                Toast.makeText(context, "수집 작업 텍스트 업로드 실패", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void userInputData() throws Exception {
            JSONObject userInput = new JSONObject();
            JSONArray setArray = new JSONArray();

            for(int i=0;i<questionList.size();i++) {
                JSONObject setObject = new JSONObject();
                setObject.put("question",questionList.get(i).getText().toString());
                setObject.put("answer",answerList.get(i).getText().toString());
                setArray.add(setObject);
            }
            userInput.put("set",setArray);
            String dirPath = context.getCacheDir().getAbsolutePath() + "/freitag";
            File dir = new File(dirPath);
            if(!dir.exists()) {
                dir.mkdir();
            }
            String fileName = project.getProjectName()+randomAlphaNumeric(6)+".txt";
            String filePath= dirPath + "/"+ fileName;

            FileWriter file = new FileWriter(filePath);
            file.write(userInput.toJSONString());
            file.flush();
            file.close();
            File f = new File(filePath);
            fileNameList.add(fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.fromFile(f));
            inputStreamList.add(inputStream);
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public String getFileNameToUri(Uri data) {
        String[] proj = {MediaStore.Files.FileColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        return imgName;
    }

    public void progressON(Activity activity, String message) {

        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {

            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.layout_dialog);
            progressDialog.show();

        }


        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }


    }

    public void progressSET(String message) {

        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }

    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLLECTION_TEXT_REQUEST_CODE) {
            exampleDataUri = data.getData();
            classList.add(classname);
            System.out.println(classname + ":" + exampleDataUri);
                    try {
                        InputStream inputStream = context.getContentResolver().openInputStream(exampleDataUri);
                        inputStreamList.add(inputStream);
                        String fileName = getFileNameToUri(exampleDataUri);
                        dataURI.setText(dataURI.getText() + "\n" + fileName);
                        fileNameList.add(fileName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
        } else if (requestCode == POP_UP_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                finish();
                startActivity(getIntent());
            } else {
                finish();
            }
        }
    }
}
