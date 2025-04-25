import axios from 'axios';
import { User, Student, Teacher, Course, Grade, CourseRegistration, Notification } from './types';

const API_BASE_URL = 'https://buff53baqh.execute-api.us-east-1.amazonaws.com/prod';

const api = {
  // Auth
  setAuthToken: (token: string | null) => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common['Authorization'];
    }
  },

  // User endpoints
  listUsers: async () => {
    const response = await axios.get(`${API_BASE_URL}/users`);
    return response.data as User[];
  },

  createUser: async (userData: Omit<User, 'id'>) => {
    const response = await axios.post(`${API_BASE_URL}/users`, userData);
    return response.data;
  },

  updateUser: async (userId: string, userData: Partial<User>) => {
    const response = await axios.put(`${API_BASE_URL}/users/${userId}`, userData);
    return response.data;
  },

  deleteUser: async (userId: string) => {
    await axios.delete(`${API_BASE_URL}/users/${userId}`);
  },

  // Teacher endpoints
  createTeacher: async (teacherData: Omit<Teacher, 'id'>) => {
    const response = await axios.post(`${API_BASE_URL}/teachers`, teacherData);
    return response.data;
  },

  updateTeacher: async (teacherId: string, teacherData: Partial<Teacher>) => {
    const response = await axios.put(`${API_BASE_URL}/teachers/${teacherId}`, teacherData);
    return response.data;
  },

  deleteTeacher: async (teacherId: string) => {
    await axios.delete(`${API_BASE_URL}/teachers/${teacherId}`);
  },

  getTeacher: async (teacherId: string) => {
    const response = await axios.get(`${API_BASE_URL}/teachers/${teacherId}`);
    return response.data as Teacher;
  },

  listTeachers: async () => {
    const response = await axios.get(`${API_BASE_URL}/teachers`);
    return response.data as Teacher[];
  },

  getTeacherCourses: async (teacherId: string) => {
    const response = await axios.get(`${API_BASE_URL}/teachers/${teacherId}/courses`);
    return response.data as Course[];
  },

  getPendingRegistrations: async (teacherId: string) => {
    const response = await axios.get(`${API_BASE_URL}/teachers/${teacherId}/pending-registrations`);
    return response.data as CourseRegistration[];
  },

  // Course endpoints
  getCourses: async () => {
    const response = await axios.get(`${API_BASE_URL}/courses`);
    return response.data as Course[];
  },

  createCourse: async (courseData: Omit<Course, 'id'>) => {
    const response = await axios.post(`${API_BASE_URL}/courses`, courseData);
    return response.data;
  },

  updateCourse: async (courseId: string, courseData: Partial<Course>) => {
    const response = await axios.put(`${API_BASE_URL}/courses/${courseId}`, courseData);
    return response.data;
  },

  deleteCourse: async (courseId: string) => {
    await axios.delete(`${API_BASE_URL}/courses/${courseId}`);
  },

  // Grade endpoints
  getGrades: async (studentId: string) => {
    const response = await axios.get(`${API_BASE_URL}/grades/by-student/${studentId}`);
    return response.data as Grade[];
  },

  getCourseGrades: async (courseId: string) => {
    const response = await axios.get(`${API_BASE_URL}/grades/by-course/${courseId}`);
    return response.data as Grade[];
  },

  submitGrade: async (gradeData: Omit<Grade, 'id' | 'submittedAt'>) => {
    const response = await axios.post(`${API_BASE_URL}/grades`, gradeData);
    return response.data;
  },

  // Course registration endpoints
  submitCourseRegistration: async (courseId: string, studentId: string) => {
    const response = await axios.post(`${API_BASE_URL}/registrations`, {
      courseId,
      studentId,
    });
    return response.data as CourseRegistration;
  },

  getStudentRegistrations: async (studentId: string) => {
    const response = await axios.get(`${API_BASE_URL}/registrations/by-student/${studentId}`);
    return response.data as CourseRegistration[];
  },

  updateRegistrationStatus: async (
    registrationId: string,
    status: 'approved' | 'rejected'
  ) => {
    const response = await axios.put(
      `${API_BASE_URL}/registrations/${registrationId}`,
      { status }
    );
    return response.data;
  },

  // Notification endpoints
  getStudentNotifications: async (studentId: string) => {
    const response = await axios.get(`${API_BASE_URL}/notifications/by-student/${studentId}`);
    return response.data as Notification[];
  },
};

export default api; 