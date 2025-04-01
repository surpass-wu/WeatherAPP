package com.example.weather.DB.LessonBean;

import java.util.List;

public class LessonBean {

    private int unitCounts;
    private List<List<ActivitiesDTO>> activities;
    private int year;
    private boolean endAtSat;
    private List<String> marshalContents;
    private int unitCount;

    public int getUnitCounts() {
        return unitCounts;
    }

    public void setUnitCounts(int unitCounts) {
        this.unitCounts = unitCounts;
    }

    public List<List<ActivitiesDTO>> getActivities() {
        return activities;
    }

    public void setActivities(List<List<ActivitiesDTO>> activities) {
        this.activities = activities;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isEndAtSat() {
        return endAtSat;
    }

    public void setEndAtSat(boolean endAtSat) {
        this.endAtSat = endAtSat;
    }

    public List<String> getMarshalContents() {
        return marshalContents;
    }

    public void setMarshalContents(List<String> marshalContents) {
        this.marshalContents = marshalContents;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
    }

    public static class ActivitiesDTO {
        private String teacherId;
        private String teacherName;
        private String courseId;
        private String courseName;
        private String roomId;
        private String roomName;
        private String vaildWeeks;
        private Object taskId;
        private Object remark;
        private String assistantName;
        private String experiItemName;
        private String schGroupNo;

        public String getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(String teacherId) {
            this.teacherId = teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getVaildWeeks() {
            return vaildWeeks;
        }

        public void setVaildWeeks(String vaildWeeks) {
            this.vaildWeeks = vaildWeeks;
        }

        public Object getTaskId() {
            return taskId;
        }

        public void setTaskId(Object taskId) {
            this.taskId = taskId;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }

        public String getAssistantName() {
            return assistantName;
        }

        public void setAssistantName(String assistantName) {
            this.assistantName = assistantName;
        }

        public String getExperiItemName() {
            return experiItemName;
        }

        public void setExperiItemName(String experiItemName) {
            this.experiItemName = experiItemName;
        }

        public String getSchGroupNo() {
            return schGroupNo;
        }

        public void setSchGroupNo(String schGroupNo) {
            this.schGroupNo = schGroupNo;
        }
    }
}
