import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const AdminDashboard = () => {
    const { token } = useAuth();
    const [activeTab, setActiveTab] = useState('teachers'); // 'teachers' | 'requests' | 'depts'

    // State for Teachers
    const [teachers, setTeachers] = useState([]);
    const [newTeacher, setNewTeacher] = useState({
        name: '', email: '', password: '', phoneNumber: '', assignedDepartments: []
    });

    // State for Departments
    const [departments, setDepartments] = useState([]);
    const [newDept, setNewDept] = useState({ name: '', fullName: '' });

    // State for Requests
    const [requests, setRequests] = useState([]);

    // Fetch Data
    useEffect(() => {
        if (activeTab === 'teachers') {
            fetch('http://localhost:8080/api/admin/teachers', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
                .then(res => res.json())
                .then(data => setTeachers(data));
        } else if (activeTab === 'requests') {
            fetch('http://localhost:8080/api/admin/deletion-requests', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
                .then(res => res.json())
                .then(data => setRequests(data));
        } else if (activeTab === 'depts' || activeTab === 'teachers') {
            fetch('http://localhost:8080/api/admin/departments', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
                .then(res => res.json())
                .then(data => setDepartments(data));
        }
    }, [activeTab]);

    const handleAddTeacher = async (e) => {
        e.preventDefault();
        const res = await fetch('http://localhost:8080/api/admin/add-teacher', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(newTeacher)
        });
        if (res.ok) {
            alert('Teacher Added!');
            setNewTeacher({ name: '', email: '', password: '', phoneNumber: '', assignedDepartments: [] });
            setActiveTab('teachers');
        } else alert('Error adding teacher');
    };

    const handleAddDept = async (e) => {
        e.preventDefault();
        const res = await fetch('http://localhost:8080/api/admin/add-department', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(newDept)
        });
        if (res.ok) {
            alert('Department Added!');
            setNewDept({ name: '', fullName: '' });
            // Refresh
            fetch('http://localhost:8080/api/admin/departments', { headers: { 'Authorization': `Bearer ${token}` } })
                .then(res => res.json()).then(data => setDepartments(data));
        }
    };

    const deleteDept = async (id) => {
        await fetch(`http://localhost:8080/api/admin/delete-department/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        setDepartments(departments.filter(d => d.id !== id));
    };

    const toggleDeptSelection = (deptName) => {
        const current = newTeacher.assignedDepartments;
        if (current.includes(deptName)) {
            setNewTeacher({ ...newTeacher, assignedDepartments: current.filter(d => d !== deptName) });
        } else {
            setNewTeacher({ ...newTeacher, assignedDepartments: [...current, deptName] });
        }
    };

    const toggleStatus = async (id) => {
        await fetch(`http://localhost:8080/api/admin/toggle-user/${id}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        setTeachers(teachers.map(t => t.id === id ? { ...t, enabled: !t.enabled } : t));
    };

    const deleteTeacher = async (id) => {
        if (!window.confirm("Delete this teacher? This will NOT delete their notes (soft delete teacher only).")) return;
        await fetch(`http://localhost:8080/api/admin/delete-user/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        setTeachers(teachers.filter(t => t.id !== id));
    };

    const handleAction = async (id, action) => {
        await fetch(`http://localhost:8080/api/admin/${action}-delete/${id}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        setRequests(requests.filter(r => r.id !== id));
    };

    return (
        <div className="p-8 max-w-7xl mx-auto min-h-screen">
            <h1 className="text-3xl font-black mb-6 text-gray-800 dark:text-white">Admin Control Center</h1>

            {/* TABS */}
            <div className="flex gap-8 mb-8 border-b dark:border-gray-700 overflow-x-auto pb-1">
                <button onClick={() => setActiveTab('teachers')} className={`pb-4 text-xs font-bold uppercase tracking-widest whitespace-nowrap transition-all ${activeTab === 'teachers' ? 'border-b-4 border-blue-600 text-blue-600' : 'text-gray-400 hover:text-gray-600'}`}>Staff Directory</button>
                <button onClick={() => setActiveTab('depts')} className={`pb-4 text-xs font-bold uppercase tracking-widest whitespace-nowrap transition-all ${activeTab === 'depts' ? 'border-b-4 border-blue-600 text-blue-600' : 'text-gray-400 hover:text-gray-600'}`}>Departments & Hierarchy</button>
                <button onClick={() => setActiveTab('requests')} className={`pb-4 text-xs font-bold uppercase tracking-widest whitespace-nowrap transition-all ${activeTab === 'requests' ? 'border-b-4 border-blue-600 text-blue-600' : 'text-gray-400 hover:text-gray-600'}`}>Deletion Requests</button>
            </div>

            {activeTab === 'teachers' && (
                <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                    {/* ADD TEACHER FORM */}
                    <div className="bg-white dark:bg-gray-800 p-6 rounded-3xl shadow-xl border border-gray-100 dark:border-gray-700 h-fit">
                        <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
                            <span className="w-8 h-8 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center font-black">+</span>
                            Hire Teacher
                        </h2>
                        <form onSubmit={handleAddTeacher} className="space-y-4">
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase tracking-tighter">Full Name</label>
                                <input value={newTeacher.name} placeholder="e.g. Dr. Ramesh" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl mt-1 text-sm outline-none focus:ring-2 focus:ring-blue-500/20" onChange={e => setNewTeacher({ ...newTeacher, name: e.target.value })} required />
                            </div>
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase tracking-tighter">Email Address</label>
                                <input value={newTeacher.email} placeholder="teacher@smvec.ac.in" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl mt-1 text-sm outline-none focus:ring-2 focus:ring-blue-500/20" onChange={e => setNewTeacher({ ...newTeacher, email: e.target.value })} required />
                            </div>
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase tracking-tighter">Phone Number</label>
                                <input value={newTeacher.phoneNumber} placeholder="+91 98765 43210" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl mt-1 text-sm outline-none focus:ring-2 focus:ring-blue-500/20" onChange={e => setNewTeacher({ ...newTeacher, phoneNumber: e.target.value })} required />
                            </div>
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase tracking-tighter">Password</label>
                                <input value={newTeacher.password} type="password" placeholder="••••••••" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl mt-1 text-sm outline-none focus:ring-2 focus:ring-blue-500/20" onChange={e => setNewTeacher({ ...newTeacher, password: e.target.value })} required />
                            </div>
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase tracking-tighter mb-2 block">Assigned Departments</label>
                                <div className="grid grid-cols-2 gap-2">
                                    {departments.map(d => (
                                        <button
                                            key={d.id}
                                            type="button"
                                            onClick={() => toggleDeptSelection(d.name)}
                                            className={`text-[10px] font-bold px-2 py-1.5 rounded-lg border transition-all ${newTeacher.assignedDepartments.includes(d.name) ? 'bg-blue-600 border-blue-600 text-white shadow-lg shadow-blue-500/20' : 'bg-gray-50 dark:bg-gray-700 border-gray-200 dark:border-gray-600 text-gray-500'}`}
                                        >
                                            {d.name.toUpperCase()}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <button className="bg-blue-600 text-white font-black py-4 rounded-2xl w-full hover:bg-blue-700 transition shadow-xl shadow-blue-500/20 active:scale-95 mt-4">Create Account</button>
                        </form>
                    </div>

                    {/* TEACHERS LIST */}
                    <div className="lg:col-span-3 space-y-4">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold">Staff Members</h2>
                            <span className="bg-gray-100 dark:bg-gray-800 text-[10px] font-bold px-3 py-1 rounded-full">{teachers.length} Active Users</span>
                        </div>
                        <div className="bg-white dark:bg-gray-800 rounded-3xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700">
                            <table className="w-full text-left">
                                <thead className="bg-gray-50 dark:bg-gray-900/50 border-b dark:border-gray-700">
                                    <tr>
                                        <th className="p-6 text-[10px] font-black text-gray-400 uppercase tracking-tighter">Avatar & Info</th>
                                        <th className="p-6 text-[10px] font-black text-gray-400 uppercase tracking-tighter font-serif">Contact</th>
                                        <th className="p-6 text-[10px] font-black text-gray-400 uppercase tracking-tighter">Departments</th>
                                        <th className="p-6 text-[10px] font-black text-gray-400 uppercase tracking-tighter text-right">Status</th>
                                        <th className="p-6 text-[10px] font-black text-gray-400 uppercase tracking-tighter text-right">Action</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y dark:divide-gray-700">
                                    {teachers.map(t => (
                                        <tr key={t.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition">
                                            <td className="p-6">
                                                <div className="flex items-center gap-4">
                                                    <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 flex items-center justify-center font-black text-gray-500">
                                                        {t.name[0]}
                                                    </div>
                                                    <div>
                                                        <p className="font-bold text-sm">{t.name}</p>
                                                        <p className="text-xs text-gray-400">{t.email}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="p-6">
                                                <p className="text-xs font-mono text-gray-500">{t.phoneNumber || 'N/A'}</p>
                                            </td>
                                            <td className="p-6">
                                                <div className="flex flex-wrap gap-1">
                                                    {t.assignedDepartments?.map(d => (
                                                        <span key={d} className="text-[10px] font-black uppercase bg-blue-50 dark:bg-blue-900/40 text-blue-600 px-2 py-0.5 rounded-lg border border-blue-100">
                                                            {d}
                                                        </span>
                                                    ))}
                                                </div>
                                            </td>
                                            <td className="p-6 text-right">
                                                <button onClick={() => toggleStatus(t.id)} className={`px-4 py-1.5 rounded-xl text-[10px] font-black uppercase transition-all active:scale-95 ${t.enabled ? 'bg-green-100 text-green-600 hover:bg-green-200' : 'bg-red-100 text-red-600 hover:bg-red-200'}`}>
                                                    {t.enabled ? 'Active' : 'Locked'}
                                                </button>
                                            </td>
                                            <td className="p-6 text-right">
                                                <button onClick={() => deleteTeacher(t.id)} className="w-8 h-8 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-400 hover:text-red-500 flex items-center justify-center transition hover:bg-red-50">
                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'depts' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-xl border dark:border-gray-700 h-fit">
                        <h2 className="text-xl font-bold mb-6">Create Department</h2>
                        <form onSubmit={handleAddDept} className="space-y-4">
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase mb-1 block">Short Name (Slug)</label>
                                <input value={newDept.name} placeholder="e.g. mca, cse" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl text-sm" onChange={e => setNewDept({ ...newDept, name: e.target.value.toLowerCase() })} required />
                            </div>
                            <div>
                                <label className="text-[10px] font-black text-gray-400 uppercase mb-1 block">Full Department Name</label>
                                <input value={newDept.fullName} placeholder="e.g. Computer Applications" className="w-full border dark:border-gray-600 dark:bg-gray-700 p-3 rounded-xl text-sm" onChange={e => setNewDept({ ...newDept, fullName: e.target.value })} required />
                            </div>
                            <button className="bg-blue-600 text-white font-black py-4 rounded-xl w-full hover:bg-blue-700 transition">Add Folder</button>
                        </form>
                    </div>

                    <div className="space-y-4">
                        <h2 className="text-xl font-bold mb-6">Existing Hierarchy</h2>
                        {departments.map(d => (
                            <div key={d.id} className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-xl flex justify-between items-center border dark:border-gray-700 group">
                                <div className="flex items-center gap-4">
                                    <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center font-black">
                                        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" /></svg>
                                    </div>
                                    <div>
                                        <p className="font-bold">{d.fullName}</p>
                                        <p className="text-xs text-gray-400 uppercase font-black tracking-widest">{d.name}</p>
                                    </div>
                                </div>
                                <button onClick={() => deleteDept(d.id)} className="w-10 h-10 rounded-xl bg-gray-50 dark:bg-gray-700 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition flex items-center justify-center">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {activeTab === 'requests' && (
                <div className="space-y-4 max-w-4xl">
                    {requests.length === 0 && <div className="p-12 text-center text-gray-400 font-medium bg-gray-50 dark:bg-gray-800 rounded-3xl">No pending deletion requests.</div>}
                    {requests.map(req => (
                        <div key={req.id} className="bg-white dark:bg-gray-800 p-8 rounded-3xl shadow-xl flex justify-between items-center border border-l-8 border-l-red-500">
                            <div>
                                <div className="flex items-center gap-2 mb-2">
                                    <span className="bg-red-50 text-red-600 text-[10px] font-bold px-2 py-0.5 rounded-lg uppercase">Security Approval Required</span>
                                    <span className="text-xs text-gray-400">ID: #{req.id}</span>
                                </div>
                                <p className="font-black text-xl text-gray-800 dark:text-white">{req.note?.title}</p>
                                <p className="text-sm text-gray-500 mt-1 italic">Reason: "{req.reason}"</p>
                            </div>
                            <div className="flex flex-col gap-2">
                                <button onClick={() => handleAction(req.id, 'approve')} className="bg-red-600 text-white px-8 py-3 rounded-2xl font-black hover:bg-red-700 transition shadow-lg shadow-red-500/20 active:scale-95">Accept Delete</button>
                                <button onClick={() => handleAction(req.id, 'reject')} className="bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-8 py-3 rounded-2xl font-black hover:bg-gray-200 transition active:scale-95">Ignore</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default AdminDashboard;