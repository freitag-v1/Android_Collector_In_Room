package com.ajou.capstone_design_freitag.Work;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.ajou.capstone_design_freitag.API.RESTAPI;
import com.ajou.capstone_design_freitag.R;
import com.ajou.capstone_design_freitag.UI.dto.ProblemWithClass;
import com.ajou.capstone_design_freitag.UI.dto.Project;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoundingBoxPagerAdapter  extends androidx.viewpager.widget.PagerAdapter  {

    private BoundingBoxActivity boundingBoxActivity;
    private static Context mContext;
    private List<ProblemWithClass> problemList;
    static File file;
    OutputStream outputStream;

    static TextView projectName;
    static TextView wayContent;
    static TextView conditionContent;
    static TextView requester;
    static TextView classListView;
    static ImageView exampleContent;
    static ImageView boundingImage;

    Project project;
    String label = null;
    static Map<Integer,Uri> positionUri = new HashMap<>();
    static int currentPosition;

    private OnRadioCheckedChanged mOnRadioCheckedChanged;
    private RegisterListener mregisterListener;

    public BoundingBoxPagerAdapter(BoundingBoxActivity boundingBoxActivity, List<ProblemWithClass> problemWithClassList,
                                   Project projectInfo, OnRadioCheckedChanged onRadioCheckedChanged, RegisterListener registerListener){
        this.boundingBoxActivity = boundingBoxActivity;
        mContext = boundingBoxActivity.getApplicationContext();
        project = projectInfo;
        this.mOnRadioCheckedChanged = onRadioCheckedChanged;
        this.mregisterListener = registerListener;
        if(problemWithClassList==null){
            problemList = new ArrayList<>();
        }
        else{
            problemList = problemWithClassList;
        }
    }

    //activity로 데이터 주기 위한 interface들 구현
    public interface OnRadioCheckedChanged {
        void onRadioCheckedChanged(String label,int problemId);
    }
    public interface RegisterListener {
        void clickBtn();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (position==0){ //문제 시작 전 설명 화면
            View view;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_start_boundingbox_problem, container, false);

            projectName = view.findViewById(R.id.boundingbox_project_name);
            wayContent = view.findViewById(R.id.boundingbox_way_content);
            conditionContent = view.findViewById(R.id.boundingbox_condition_content);
            requester = view.findViewById(R.id.boundingbox_requester);
            classListView = view.findViewById(R.id.boundingbox_classlist);
            exampleContent = view.findViewById(R.id.boundingbox_example_content);
            projectName.setText(project.getProjectName());
            wayContent.setText(project.getWayContent());
            conditionContent.setText(project.getConditionContent());
            requester.setText(project.getUserId());
            classListView.setText(project.getClass_list().toString());

            BoundingBoxPagerAdapter.DownloadDataTask downloadDataTask = new BoundingBoxPagerAdapter.DownloadDataTask();
            downloadDataTask.execute(project.getBucketName(),project.getExampleContent(),outputStream,"예시");

            container.addView(view);
            return view;
        }
        else {
            View view = null;
            if (mContext != null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_boundingbox_problem_set, container, false);

                TextView work_num = view.findViewById(R.id.boundingbox_work_num);
                work_num.setText(Integer.toString(position));
                TextView problem_num = view.findViewById(R.id.boundingbox_problem_num);
                problem_num.setText(Integer.toString(problemList.get(position-1).getProblem().getProblemId()));
                boundingImage = view.findViewById(R.id.boundingbox_image);

                RadioGroup classList = view.findViewById(R.id.boundingbox_radio_group);
                for (int i = 0; i < problemList.get(position-1).getClassNameList().size(); i++) {
                    final RadioButton radioButton = new RadioButton(mContext);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    radioButton.setLayoutParams(param);
                    radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentPosition = position;
                            label = radioButton.getText().toString();
                        }
                    });

                    radioButton.setText(problemList.get(position-1).getClassNameList().get(i).getClassName());
                    radioButton.setId(i);
                    classList.addView(radioButton);
                }

                String file_extension = FilenameUtils.getExtension(problemList.get(position-1).getProblem().getObjectName());
                file = new File("/data/data/com.ajou.capstone_design_freitag/files/project_boundingbox"+position+"." + file_extension);
                positionUri.put(position,Uri.fromFile(file));
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                getBoundingBoxData(position, "바운딩박스");
            }
            container.addView(view);

            ImageView boundingBoxStart = view.findViewById(R.id.boundingbox_start);
            boundingBoxStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnRadioCheckedChanged.onRadioCheckedChanged(label,problemList.get(position-1).getProblem().getProblemId());
                    CropImage.activity(positionUri.get(position))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setActivityTitle("My Crop")
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setCropMenuCropButtonTitle("Done")
                            .setCropMenuCropButtonIcon(R.drawable.boundingbox)
                            .setAsBoundingBoxSelector()
                            .start(boundingBoxActivity);
                }
            });

            Button next = view.findViewById(R.id.boundingbox_upload);
            Button done = view.findViewById(R.id.boundingbox_done);
            done.setVisibility(View.GONE);
            if (position ==5){
                done.setVisibility(View.VISIBLE);
            }
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mregisterListener.clickBtn();
                }
            });
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boundingBoxActivity.finish();
                }
            });
            return view;
        }
    }

    private void getBoundingBoxData(int position, String dataType) {
        BoundingBoxPagerAdapter.DownloadDataTask downloadDataTask = new BoundingBoxPagerAdapter.DownloadDataTask();
        downloadDataTask.execute(problemList.get(position-1).getProblem().getBucketName(),problemList.get(position-1).getProblem().getObjectName(),outputStream,dataType);
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

    private static class DownloadDataTask extends AsyncTask<Object, Void, Boolean>{
        String dataType;
        protected Boolean doInBackground(Object... dataInfos) {
            Boolean result = RESTAPI.getInstance().downloadObject((String)dataInfos[0],(String)dataInfos[1],(OutputStream)dataInfos[2]);
            dataType = (String)dataInfos[3];
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
                if(dataType.equals("바운딩박스")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream).copy(Bitmap.Config.ARGB_8888, true);
                    //bitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
                    boundingImage.setImageBitmap(bitmap);
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(dataType.equals("예시")){
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    exampleContent.setImageBitmap(bitmap);
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}