export interface User {
  id: string;
  email: string;
  name: string;
  role: 'admin' | 'teacher' | 'student';
}

export interface Student {
  id: string;
  name: string;
  email: string;
  major: string;
  enrollmentYear: number;
  graduationYear: number;
}

export interface Teacher {
  id: string;
  name: string;
  email: string;
  department: string;
  courses: string[]; // Course IDs
}

export interface Course {
  id: string;
  code: string;
  name: string;
  description: string;
  credits: number;
  semester: string;
  instructor: string;
  createdAt: string;
  updatedAt: string;
}

export interface Grade {
  id: string;
  courseId: string;
  studentId: string;
  grade: string;
  semester: string;
  submittedBy: string; // Teacher ID
  submittedAt: string;
}

export interface CourseRegistration {
  id: string;
  courseId: string;
  studentId: string;
  status: 'pending' | 'approved' | 'rejected';
  requestedAt: string;
  updatedAt: string;
}

export interface Notification {
  id: string;
  studentId: string;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
} 