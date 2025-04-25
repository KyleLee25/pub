import React, { useState, useEffect } from 'react';
import { User, Student, Teacher, Course } from '../../api/types';
import api from '../../api/api';
import { useAuth } from '../../contexts/AuthContext';
import './Dashboard.css';
import axios from 'axios';

interface CreateUserForm {
  name: string;
  email: string;
  password: string;
  role: 'admin' | 'teacher' | 'student';
}

interface CreateCourseForm {
  name: string;
  code: string;
  description: string;
  credits: number;
  semester: string;
  instructor: string;
  createdAt: string;
  updatedAt: string;
}

interface CreateTeacherForm {
  name: string;
  email: string;
  password: string;
  department: string;
  courses: string[];
}

const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'users' | 'courses' | 'teachers'>('users');
  const [users, setUsers] = useState<User[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [createUserForm, setCreateUserForm] = useState<CreateUserForm>({
    name: '',
    email: '',
    password: '',
    role: 'student'
  });
  const [createCourseForm, setCreateCourseForm] = useState<CreateCourseForm>({
    name: '',
    code: '',
    description: '',
    credits: 0,
    semester: '',
    instructor: '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  });
  const [createTeacherForm, setCreateTeacherForm] = useState<CreateTeacherForm>({
    name: '',
    email: '',
    password: '',
    department: '',
    courses: []
  });
  const { logout } = useAuth();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [usersData, coursesData, teachersData] = await Promise.all([
        api.listUsers(),
        api.getCourses(),
        api.listTeachers()
      ]);
      setUsers(usersData);
      setCourses(coursesData);
      setTeachers(teachersData);
    } catch (error) {
      console.error('Error loading admin data:', error);
    }
  };

  const handleLogout = () => {
    logout();
  };

  const handleCreate = async () => {
    try {
      console.log('Starting create with activeTab:', activeTab);

      if (activeTab === 'users') {
        console.log('Creating user with data:', createUserForm);
        try {
          const response = await api.createUser(createUserForm);
          console.log('Create response:', response);
          setShowCreateModal(false);
          // Reset the form
          setCreateUserForm({
            name: '',
            email: '',
            password: '',
            role: 'student'
          });
          await loadData(); // Refresh the data
        } catch (error: any) {
          console.error('Failed to create user:', {
            error: error.message,
            response: error.response?.data,
            status: error.response?.status
          });
        }
      } else {
        switch (activeTab) {
          case 'courses':
            await api.createCourse(createCourseForm);
            break;
          case 'teachers':
            await api.createTeacher(createTeacherForm);
            break;
        }
        setShowCreateModal(false);
        await loadData();
      }
    } catch (error) {
      console.error('Error in handleCreate:', error);
    }
  };

  const handleEdit = async () => {
    try {
      console.log('Starting edit with activeTab:', activeTab);
      console.log('Selected item:', selectedItem);

      if (!selectedItem?.id) {
        console.error('No item ID found');
        return;
      }

      if (activeTab === 'users') {
        const userData = {
          name: selectedItem.name,
          email: selectedItem.email,
          role: selectedItem.role
        };
        console.log('Updating user with data:', userData);
        
        try {
          const response = await api.updateUser(selectedItem.id, userData);
          console.log('Update response:', response);
          setShowEditModal(false);
          await loadData(); // Refresh the data
        } catch (error: any) {
          console.error('Failed to update user:', {
            error: error.message,
            response: error.response?.data,
            status: error.response?.status
          });
        }
      } else {
        // Handle other tabs...
        switch (activeTab) {
          case 'courses':
            await api.updateCourse(selectedItem.id, selectedItem);
            break;
          case 'teachers':
            await api.updateTeacher(selectedItem.id, selectedItem);
            break;
        }
        setShowEditModal(false);
        await loadData();
      }
    } catch (error) {
      console.error('Error in handleEdit:', error);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      console.log('Attempting to delete:', { activeTab, id });
      
      if (activeTab === 'users') {
        try {
          await api.deleteUser(id);
          console.log('User deleted successfully');
        } catch (error: any) {
          console.error('Failed to delete user:', {
            error: error.message,
            response: error.response?.data,
            status: error.response?.status,
            headers: error.response?.headers
          });
          return;
        }
      } else {
        switch (activeTab) {
          case 'courses':
            await api.deleteCourse(id);
            break;
          case 'teachers':
            await api.deleteTeacher(id);
            break;
        }
      }
      
      await loadData(); // Refresh the data
    } catch (error) {
      console.error('Error in handleDelete:', error);
    }
  };

  const renderCreateModal = () => {
    return (
      <div className="modal">
        <div className="modal-content">
          <h2>Create New {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}</h2>
          {activeTab === 'users' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={createUserForm.name}
                onChange={(e) => setCreateUserForm({ ...createUserForm, name: e.target.value })}
              />
              <input
                type="email"
                placeholder="Email"
                value={createUserForm.email}
                onChange={(e) => setCreateUserForm({ ...createUserForm, email: e.target.value })}
              />
              <input
                type="password"
                placeholder="Password"
                value={createUserForm.password}
                onChange={(e) => setCreateUserForm({ ...createUserForm, password: e.target.value })}
              />
              <select
                value={createUserForm.role}
                onChange={(e) => setCreateUserForm({ ...createUserForm, role: e.target.value as any })}
              >
                <option value="student">Student</option>
                <option value="teacher">Teacher</option>
                <option value="admin">Admin</option>
              </select>
            </div>
          )}
          {activeTab === 'courses' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={createCourseForm.name}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, name: e.target.value })}
              />
              <input
                type="text"
                placeholder="Code"
                value={createCourseForm.code}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, code: e.target.value })}
              />
              <input
                type="text"
                placeholder="Description"
                value={createCourseForm.description}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, description: e.target.value })}
              />
              <input
                type="number"
                placeholder="Credits"
                value={createCourseForm.credits}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, credits: parseInt(e.target.value) })}
              />
              <input
                type="text"
                placeholder="Semester"
                value={createCourseForm.semester}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, semester: e.target.value })}
              />
              <select
                value={createCourseForm.instructor}
                onChange={(e) => setCreateCourseForm({ ...createCourseForm, instructor: e.target.value })}
              >
                <option value="">Select Instructor</option>
                {teachers.map(teacher => (
                  <option key={teacher.id} value={teacher.id}>{teacher.name}</option>
                ))}
              </select>
            </div>
          )}
          {activeTab === 'teachers' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={createTeacherForm.name}
                onChange={(e) => setCreateTeacherForm({ ...createTeacherForm, name: e.target.value })}
              />
              <input
                type="email"
                placeholder="Email"
                value={createTeacherForm.email}
                onChange={(e) => setCreateTeacherForm({ ...createTeacherForm, email: e.target.value })}
              />
              <input
                type="password"
                placeholder="Password"
                value={createTeacherForm.password}
                onChange={(e) => setCreateTeacherForm({ ...createTeacherForm, password: e.target.value })}
              />
              <input
                type="text"
                placeholder="Department"
                value={createTeacherForm.department}
                onChange={(e) => setCreateTeacherForm({ ...createTeacherForm, department: e.target.value })}
              />
            </div>
          )}
          <div className="modal-actions">
            <button onClick={() => setShowCreateModal(false)}>Cancel</button>
            <button onClick={handleCreate}>Create</button>
          </div>
        </div>
      </div>
    );
  };

  const renderEditModal = () => {
    if (!selectedItem) return null;
    return (
      <div className="modal">
        <div className="modal-content">
          <h2>Edit {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}</h2>
          {activeTab === 'users' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={selectedItem.name}
                onChange={(e) => setSelectedItem({ ...selectedItem, name: e.target.value })}
              />
              <input
                type="email"
                placeholder="Email"
                value={selectedItem.email}
                onChange={(e) => setSelectedItem({ ...selectedItem, email: e.target.value })}
              />
              <select
                value={selectedItem.role}
                onChange={(e) => setSelectedItem({ ...selectedItem, role: e.target.value })}
              >
                <option value="student">Student</option>
                <option value="teacher">Teacher</option>
                <option value="admin">Admin</option>
              </select>
            </div>
          )}
          {activeTab === 'courses' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={selectedItem.name}
                onChange={(e) => setSelectedItem({ ...selectedItem, name: e.target.value })}
              />
              <input
                type="text"
                placeholder="Code"
                value={selectedItem.code}
                onChange={(e) => setSelectedItem({ ...selectedItem, code: e.target.value })}
              />
              <input
                type="text"
                placeholder="Description"
                value={selectedItem.description}
                onChange={(e) => setSelectedItem({ ...selectedItem, description: e.target.value })}
              />
              <input
                type="number"
                placeholder="Credits"
                value={selectedItem.credits}
                onChange={(e) => setSelectedItem({ ...selectedItem, credits: parseInt(e.target.value) })}
              />
              <input
                type="text"
                placeholder="Semester"
                value={selectedItem.semester}
                onChange={(e) => setSelectedItem({ ...selectedItem, semester: e.target.value })}
              />
              <select
                value={selectedItem.instructor}
                onChange={(e) => setSelectedItem({ ...selectedItem, instructor: e.target.value })}
              >
                <option value="">Select Instructor</option>
                {teachers.map(teacher => (
                  <option key={teacher.id} value={teacher.id}>{teacher.name}</option>
                ))}
              </select>
            </div>
          )}
          {activeTab === 'teachers' && (
            <div className="form-group">
              <input
                type="text"
                placeholder="Name"
                value={selectedItem.name}
                onChange={(e) => setSelectedItem({ ...selectedItem, name: e.target.value })}
              />
              <input
                type="email"
                placeholder="Email"
                value={selectedItem.email}
                onChange={(e) => setSelectedItem({ ...selectedItem, email: e.target.value })}
              />
              <input
                type="text"
                placeholder="Department"
                value={selectedItem.department}
                onChange={(e) => setSelectedItem({ ...selectedItem, department: e.target.value })}
              />
            </div>
          )}
          <div className="modal-actions">
            <button onClick={() => setShowEditModal(false)}>Cancel</button>
            <button onClick={handleEdit}>Save</button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Admin Dashboard</h1>
        <div className="header-actions">
          <div className="tab-navigation">
            <button
              className={activeTab === 'users' ? 'active' : ''}
              onClick={() => setActiveTab('users')}
            >
              Users
            </button>
            <button
              className={activeTab === 'courses' ? 'active' : ''}
              onClick={() => setActiveTab('courses')}
            >
              Courses
            </button>
            <button
              className={activeTab === 'teachers' ? 'active' : ''}
              onClick={() => setActiveTab('teachers')}
            >
              Teachers
            </button>
          </div>
          <button className="logout-button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="content-header">
          <h2>{activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}</h2>
          <button 
            className="create-button"
            onClick={() => setShowCreateModal(true)}
          >
            Add New {activeTab.slice(0, -1)}
          </button>
        </div>

        {activeTab === 'users' && (
          <div className="users-grid">
            {users.map((user) => (
              <div key={user.id} className="user-card">
                <h3>{user.name}</h3>
                <p>Role: {user.role}</p>
                <p>Email: {user.email}</p>
                <div className="card-actions">
                  <button onClick={() => {
                    setSelectedItem(user);
                    setShowEditModal(true);
                  }}>Edit</button>
                  <button className="danger" onClick={() => handleDelete(user.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'courses' && (
          <div className="courses-grid">
            {courses.map((course) => (
              <div key={course.id} className="course-card">
                <h3>{course.name}</h3>
                <p>Code: {course.code}</p>
                <p>Credits: {course.credits}</p>
                <p>Description: {course.description}</p>
                <p>Semester: {course.semester}</p>
                <p>Instructor: {teachers.find(t => t.id === course.instructor)?.name || 'Not assigned'}</p>
                <div className="card-actions">
                  <button onClick={() => {
                    setSelectedItem(course);
                    setShowEditModal(true);
                  }}>Edit</button>
                  <button className="danger" onClick={() => handleDelete(course.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'teachers' && (
          <div className="teachers-grid">
            {teachers.map((teacher) => (
              <div key={teacher.id} className="teacher-card">
                <h3>{teacher.name}</h3>
                <p>Email: {teacher.email}</p>
                <p>Department: {teacher.department}</p>
                <p>Courses: {teacher?.courses?.length}</p>
                <div className="card-actions">
                  <button onClick={() => {
                    setSelectedItem(teacher);
                    setShowEditModal(true);
                  }}>Edit</button>
                  <button className="danger" onClick={() => handleDelete(teacher.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showCreateModal && renderCreateModal()}
      {showEditModal && renderEditModal()}
    </div>
  );
};

export default AdminDashboard; 