package com.ajou.capstone_design_freitag.Work;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ajou.capstone_design_freitag.API.RESTAPI;
import com.ajou.capstone_design_freitag.R;
import com.ajou.capstone_design_freitag.UI.dto.ProblemWithClass;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class ClassificationPagerAdapter extends androidx.viewpager.widget.PagerAdapter {

    // LayoutInflater 서비스 사용을 위한 Context 참조 저장.
    private ClassificationActivity classificationActivity;
    private static Context mContext;
    private static List<ProblemWithClass> problemList;
    //다운로드
    String file_extension;
    static File file;
    OutputStream outputStream;

    //이미지
    static ImageView imageView;
    //오디오
    LinearLayout audiolayout;
    static TextView textView;
    static ImageView play;
    static ImageView pause;
    static MediaPlayer mediaPlayer = new MediaPlayer();

    //바운딩박스
    static TextView conditionView;
    static CustomView boundingBoxView;
    static LinearLayout boundingBoxQuestionLayout;

    //답제출
    static StringBuffer answer = new StringBuffer();
    static List<String> problemId = new ArrayList<>();
    static List<StringBuffer> classAnswers = new ArrayList<>();
    static int currentPage;

    public ClassificationPagerAdapter(ClassificationActivity classificationActivity, List<ProblemWithClass> problemWithClassList){
        this.classificationActivity = classificationActivity;
        mContext = classificationActivity.getApplicationContext();
        if(problemWithClassList==null){
            problemList = new ArrayList<>();
        }
        else{
            problemList = problemWithClassList;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (position==0){ //문제 시작 전 설명 화면
            View view;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_start_classification_problem, container, false);
            container.addView(view);
            return view;
        }
        else {
            View view = null;
            if (mContext != null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_classification_problem_set, container, false);

                TextView work_num = view.findViewById(R.id.classification_work_num);
                TextView problem_num = view.findViewById(R.id.classification_problem_num);
                imageView = view.findViewById(R.id.classification_image);
                imageView.setVisibility(View.GONE);

                play = view.findViewById(R.id.classification_play);
                pause = view.findViewById(R.id.classification_pause);
                audiolayout = view.findViewById(R.id.classification_audio_layout);
                audiolayout.setVisibility(View.GONE);

                textView = view.findViewById(R.id.classification_text);
                textView.setVisibility(View.GONE);

                TextView conditionText = view.findViewById(R.id.conditionText);
                conditionText.setVisibility(View.GONE);
                conditionView = view.findViewById(R.id.conditionView);
                conditionView.setVisibility(View.GONE);
                boundingBoxView = view.findViewById(R.id.boundingBoxView);
                boundingBoxView.setVisibility(View.GONE);
                boundingBoxQuestionLayout = view.findViewById(R.id.boundingQuestionLayout);
                boundingBoxQuestionLayout.setVisibility(View.GONE);
                TextView labelTextView = view.findViewById(R.id.labelTextView);

                RadioGroup classList = view.findViewById(R.id.classification_radio_group);
                for (int i = 0; i < problemList.get(position-1).getClassNameList().size()+1; i++) {
                    final RadioButton radioButton = new RadioButton(mContext);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    radioButton.setLayoutParams(param);
                    radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentPage = position;
                            answer.append(radioButton.getText().toString());
                        }
                    });
                    if (i==problemList.get(position-1).getClassNameList().size()){
                        radioButton.setText("없음");
                    }
                    else{
                        radioButton.setText(problemList.get(position-1).getClassNameList().get(i).getClassName());
                    }
                    radioButton.setId(i);
                    classList.addView(radioButton);
                }

                work_num.setText(Integer.toString(position));
                problem_num.setText(Integer.toString(problemList.get(position-1).getProblem().getProblemId()));

                file_extension = FilenameUtils.getExtension(problemList.get(position-1).getProblem().getObjectName());
                file = new File("/data/data/com.ajou.capstone_design_freitag/files/project_classification." + file_extension);
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (file_extension.equals("jpg") || file_extension.equals("png") || file_extension.equals("jpeg")) {
                    if(problemList.get(position-1).getBoundingBoxList() == null) {
                        imageView.setVisibility(View.VISIBLE);
                        audiolayout.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        getClassificationData(position, "이미지");
                    }
                    else{
                        imageView.setVisibility(View.GONE);
                        audiolayout.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        classList.setVisibility(View.GONE);
                        conditionText.setVisibility(View.VISIBLE);
                        conditionView.setVisibility(View.VISIBLE);
                        conditionView.setText(problemList.get(position-1).getConditionContent());
                        boundingBoxView.setVisibility(View.VISIBLE);
                        boundingBoxQuestionLayout.setVisibility(View.VISIBLE);
                        labelTextView.setVisibility(View.GONE);
                        getClassificationData(position, "바운딩박스");
                    }
                } else if (file_extension.equals("mp3") || file_extension.equals("wav") || file_extension.equals("m4a")) {
                        imageView.setVisibility(View.GONE);
                        audiolayout.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                        getClassificationData(position, "음성");
                } else if(file_extension.equals("txt")) {
                    imageView.setVisibility(View.GONE);
                    audiolayout.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    getClassificationData(position, "텍스트");
                }

            }
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    mediaPlayer.start();
                }
            });

            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                    mediaPlayer.pause();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    readyToPlay();
                }
            });
            container.addView(view);

            Button next = view.findViewById(R.id.classification_upload);
            Button done = view.findViewById(R.id.classification_done);
            done.setVisibility(View.GONE);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadUserAnswer(answer);
                }
            });
            if (position ==5){
                done.setVisibility(View.VISIBLE);
            }
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    classificationWorkDone();
                    classificationActivity.finish();
                }
            });

            return view;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }

public void uploadUserAnswer(StringBuffer answer) {
        if(problemId.contains(Integer.toString(problemList.get(currentPage -1).getProblem().getProblemId()))){
            problemId.remove(Integer.toString(problemList.get(currentPage -1).getProblem().getProblemId()));
            classAnswers.remove(currentPage -1);
        }
    System.out.println("현재페이지: "+currentPage);
    System.out.println("등록되는 답: "+answer.toString());
    classAnswers.add(answer);
    answer.delete(0,answer.length());
    System.out.println("아이디: "+problemList.get(currentPage -1).getProblem().getProblemId());
    problemId.add(Integer.toString(problemList.get(currentPage -1).getProblem().getProblemId()));
    Toast.makeText(mContext, "작업 등록 성공", Toast.LENGTH_LONG).show();
}

public void classificationWorkDone(){
    ClassificationTask classificationTask = new ClassificationTask();
    classificationTask.execute();
}

private static class ClassificationTask extends AsyncTask<Void,Void,Boolean>{
    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean result = null;
        try {
            result = RESTAPI.getInstance().classificationWork(classAnswers,problemId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(result !=true){
            Toast.makeText(mContext, "분류 작업 실패", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(mContext, "분류 작업 완료", Toast.LENGTH_LONG).show();
        }
    }
}

    public void getClassificationData(int position, final String dataType) {
        DownloadDataTask downloadDataTask = new DownloadDataTask();
        downloadDataTask.execute(problemList.get(position-1).getProblem().getBucketName(),problemList.get(position-1).getProblem().getObjectName(),outputStream,dataType,position);
    }

    private static class DownloadDataTask extends AsyncTask<Object, Void, Boolean>{
        String dataType;
        int position;
        protected Boolean doInBackground(Object... dataInfos) {
            Boolean result = RESTAPI.getInstance().downloadObject((String)dataInfos[0],(String)dataInfos[1],(OutputStream)dataInfos[2]);
            dataType = (String)dataInfos[3];
            position = (int)dataInfos[4];
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                System.out.println("문제 데이터 다운로드 실패");
            }
            else
            {
                System.out.println("문제 데이터 다운로드 성공");
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if(dataType.equals("이미지")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);
                    System.out.println("bitmap: "+bitmap);
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(dataType.equals("음성")){
                    readyToPlay();
                }
                else if(dataType.equals("텍스트")){
                    try {
                        jsonParse(inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(dataType.equals("바운딩박스")){
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream).copy(Bitmap.Config.ARGB_8888,true);
                    boundingBoxView.setBitmap(bitmap
                    );
                    List<String> labelList = new ArrayList<>();
                    List<String> rectList = new ArrayList<>();

                    for (int i = 0; i < problemList.get(position-1).getBoundingBoxList().size(); i++) {
                        labelList.add(Integer.toString(problemList.get(position - 1).getBoundingBoxList().get(i).getBoxId()));
                        String[] rect = problemList.get(position - 1).getBoundingBoxList().get(i).getCoordinates().split(" ");
                        for(int j=0;j<rect.length;j++){
                            rectList.add(rect[j]);
                        }
                    }
                    boundingBoxView.setLabel(labelList);
                    boundingBoxView.setRect(rectList);


                    for (int i = 0; i < problemList.get(position-1).getBoundingBoxList().size(); i++) {
                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        LinearLayout sentence1 = new LinearLayout(mContext);
                        sentence1.setOrientation(LinearLayout.HORIZONTAL);
                        sentence1.setLayoutParams(param);
                        TextView q = new TextView(mContext);
                        q.setText("Q. 위 사진 속 ");
                        q.setLayoutParams(param);
                        sentence1.addView(q);
                        final TextView boxId = new TextView(mContext);
                        boxId.setLayoutParams(param);
                        boxId.setText(Integer.toString(problemList.get(position-1).getBoundingBoxList().get(i).getBoxId()));
                        sentence1.addView(boxId);
                        TextView q2 = new TextView(mContext);
                        q2.setLayoutParams(param);
                        q2.setText("번 바운딩 박스가 주어진 작업 조건에 맞게");
                        sentence1.addView(q2);

                        LinearLayout sentence2 = new LinearLayout(mContext);
                        sentence2.setOrientation(LinearLayout.HORIZONTAL);
                        sentence2.setLayoutParams(param);
                        TextView label = new TextView(mContext);
                        label.setText(problemList.get(position-1).getBoundingBoxList().get(i).getClassName());
                        label.setLayoutParams(param);
                        sentence2.addView(label);
                        TextView q3 = new TextView(mContext);
                        q3.setText("을 포함하고 있습니까?");
                        q3.setLayoutParams(param);
                        sentence2.addView(q3);

                        boundingBoxQuestionLayout.addView(sentence1);
                        boundingBoxQuestionLayout.addView(sentence2);

                        RadioGroup answerRadioGroup = new RadioGroup(mContext);
                        answerRadioGroup.setLayoutParams(param);
                        answerRadioGroup.setOrientation(LinearLayout.HORIZONTAL);

                        final RadioButton yes = new RadioButton(mContext);
                        final RadioButton no = new RadioButton(mContext);
                        yes.setLayoutParams(param);
                        yes.setText("예");
                        no.setLayoutParams(param);
                        no.setText("아니오");

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                currentPage = position;
                                answer.append(boxId.getText().toString());
                                answer.append(" ");
                            }
                        });
                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        answerRadioGroup.addView(yes);
                        answerRadioGroup.addView(no);
                        boundingBoxQuestionLayout.addView(answerRadioGroup);
                    }
                }
            }
        }
    }

    public static void jsonParse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        StringBuffer strBuffer = new StringBuffer();
        while(true){
            try {
                if (!((line=reader.readLine())!=null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            strBuffer.append(line+"\n");
        }
        reader.close();
        inputStream.close();
        JSONParser jsonParser = new JSONParser();
        String textData = "";
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(strBuffer.toString());
            JSONArray setArray = (JSONArray) jsonObject.get("set");
            for(int i=0;i<setArray.size();i++){
                JSONObject set = (JSONObject)setArray.get(i);
                textData+="질문:"+set.get("question")+"\n";
                textData+="답:"+set.get("answer")+"\n";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        textView.setText(textData);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readyToPlay() {
        try {
            Uri uri = Uri.fromFile(file);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mContext, uri);
            mediaPlayer.prepare();
            if(mediaPlayer.isPlaying()){
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }else{
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
            }
        }
        catch (Exception e) {
            Log.e("SimplePlayer", e.getMessage());
        }
    }


}
