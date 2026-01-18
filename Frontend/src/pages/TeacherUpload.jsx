import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

import { useNavigate, useLocation } from 'react-router-dom';

const TeacherUpload = () => {
    const { user, token } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard' | 'upload'
    const [dashboardData, setDashboardData] = useState({ notes: [], requests: [] });
    const [isEditing, setIsEditing] = useState(false);
    const [departments, setDepartments] = useState([]);

    const [formData, setFormData] = useState({
        title: '',
        department: '',
        year: 'year2',
        section: 'section-a',
        subject: 'networks',
        content: '# New Note\n\nWrite your content here...',
        uploadedByUserId: user?.id
    });

    // Handle Edit Mode from Navigation
    useEffect(() => {
        if (location.state?.editMode && location.state?.note) {
            setFormData(location.state.note);
            setIsEditing(true);
            setActiveTab('upload');
        }
    }, [location]);

    // Fetch Dashboard Data & Departments
    useEffect(() => {
        if (!user) return;

        if (activeTab === 'dashboard') {
            fetch(`http://localhost:8080/api/teacher/dashboard/${user.id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            })
                .then(res => res.json())
                .then(data => setDashboardData(data));
        }

        fetch('http://localhost:8080/api/public/departments')
            .then(res => res.json())
            .then(data => {
                setDepartments(data);
                if (data.length > 0 && !formData.department) {
                    setFormData(prev => ({ ...prev, department: data[0].name }));
                }
            });
    }, [activeTab, user]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const url = isEditing
            ? `http://localhost:8080/api/teacher/notes/${formData.id}`
            : 'http://localhost:8080/api/teacher/upload';

        try {
            const res = await fetch(url, {
                method: isEditing ? 'PUT' : 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });

            if (res.ok) {
                alert(isEditing ? '✅ Note Updated!' : '✅ Note Published!');
                setIsEditing(false);
                setActiveTab('dashboard');
                setFormData({ ...formData, title: '', content: '# New Note...' });
            }
        } catch (error) {
            console.error(error);
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    if (!user) return <div className="p-10 text-center">Please Login First</div>;

    return (
        <div className="max-w-6xl mx-auto p-6 min-h-screen">

            {/* TABS */}
            <div className="flex gap-8 mb-8 border-b dark:border-gray-700">
                <button onClick={() => setActiveTab('dashboard')} className={`pb-4 text-sm font-bold uppercase tracking-wider transition-all ${activeTab === 'dashboard' ? 'border-b-4 border-blue-600 text-blue-600' : 'text-gray-400 hover:text-gray-600'}`}>My Dashboard</button>
                <button onClick={() => { setActiveTab('upload'); setIsEditing(false); }} className={`pb-4 text-sm font-bold uppercase tracking-wider transition-all ${activeTab === 'upload' ? 'border-b-4 border-blue-600 text-blue-600' : 'text-gray-400 hover:text-gray-600'}`}>
                    {isEditing ? 'Edit Note' : 'Upload New Note'}
                </button>
            </div>

            {activeTab === 'dashboard' && (
                <div className="space-y-8">
                    {/* STATS */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-sm border dark:border-gray-700">
                            <p className="text-gray-400 text-xs font-bold uppercase">Total Uploads</p>
                            <p className="text-3xl font-black mt-1">{dashboardData.notes.length}</p>
                        </div>
                        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-xl border dark:border-gray-700">
                            <p className="text-gray-400 text-xs font-bold uppercase">Pending Deletions</p>
                            <p className="text-3xl font-black mt-1 text-orange-500">{dashboardData.requests.filter(r => r.status === 'PENDING').length}</p>
                        </div>
                        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-xl border dark:border-gray-700">
                            <p className="text-gray-400 text-xs font-bold uppercase">Total Feedback</p>
                            <p className="text-3xl font-black mt-1 text-blue-500">
                                {dashboardData.notes.reduce((acc, n) => acc + (n.likes || 0) + (n.dislikes || 0), 0)}
                            </p>
                        </div>
                    </div>

                    {/* MY NOTES TABLE */}
                    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border dark:border-gray-700">
                        <table className="w-full text-left">
                            <thead className="bg-gray-50 dark:bg-gray-900/50 border-b dark:border-gray-700">
                                <tr>
                                    <th className="p-4 text-xs font-bold text-gray-400 uppercase">Note Title</th>
                                    <th className="p-4 text-xs font-bold text-gray-400 uppercase">Subject</th>
                                    <th className="p-4 text-xs font-bold text-gray-400 uppercase">Feedback</th>
                                    <th className="p-4 text-xs font-bold text-gray-400 uppercase">Status</th>
                                    <th className="p-4 text-xs font-bold text-gray-400 uppercase text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y dark:divide-gray-700">
                                {dashboardData.notes.map(note => (
                                    <tr key={note.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition">
                                        <td className="p-4">
                                            <p className="font-bold">{note.title}</p>
                                            <p className="text-xs text-gray-400">{new Date(note.createdAt).toLocaleDateString()}</p>
                                        </td>
                                        <td className="p-4 text-xs uppercase font-bold">{note.subject}</td>
                                        <td className="p-4">
                                            <div className="flex items-center gap-3">
                                                <span className="flex items-center gap-1 text-[10px] font-black text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                                                    👍 {note.likes || 0}
                                                </span>
                                                <span className="flex items-center gap-1 text-[10px] font-black text-red-600 bg-red-50 px-2 py-0.5 rounded-full">
                                                    👎 {note.dislikes || 0}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="p-4">
                                            <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${note.enabled ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'}`}>
                                                {note.enabled ? 'Live' : 'Hidden'}
                                            </span>
                                        </td>
                                        <td className="p-4 text-right">
                                            <button onClick={() => { setFormData(note); setIsEditing(true); setActiveTab('upload'); }} className="text-blue-600 hover:underline text-xs font-bold">Edit</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* RECENT REQUESTS */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-bold">Deletion History</h3>
                        {dashboardData.requests.map(req => (
                            <div key={req.id} className="bg-gray-50 dark:bg-gray-900 p-4 rounded-xl flex justify-between items-center border dark:border-gray-800">
                                <div>
                                    <p className="text-sm font-bold">{req.note?.title}</p>
                                    <p className="text-xs text-gray-400">Reason: {req.reason}</p>
                                </div>
                                <span className={`text-[10px] font-bold uppercase px-2 py-1 rounded ${req.status === 'PENDING' ? 'bg-orange-100 text-orange-600' : req.status === 'APPROVED' ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'}`}>
                                    {req.status}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {activeTab === 'upload' && (
                <form onSubmit={handleSubmit} className="space-y-6 bg-white dark:bg-gray-800 p-8 rounded-2xl shadow-xl border dark:border-gray-700">
                    <h2 className="text-2xl font-black mb-4">{isEditing ? 'Edit Note' : 'Publish New Content'}</h2>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="col-span-2">
                            <label className="label">Note Title</label>
                            <input name="title" value={formData.title} onChange={handleChange} className="input-field" placeholder="e.g. Unit 1: Introduction to Networks" required />
                        </div>

                        <div>
                            <label className="label">Department</label>
                            <select name="department" value={formData.department} onChange={handleChange} className="input-field">
                                {departments.map(d => (
                                    <option key={d.id} value={d.name}>{d.fullName}</option>
                                ))}
                                {departments.length === 0 && <option value="">No Departments Found</option>}
                            </select>
                        </div>

                        <div>
                            <label className="label">Year</label>
                            <select name="year" value={formData.year} onChange={handleChange} className="input-field">
                                <option value="year1">Year 1</option>
                                <option value="year2">Year 2</option>
                                <option value="year3">Year 3</option>
                                <option value="year4">Year 4</option>
                            </select>
                        </div>

                        <div>
                            <label className="label">Section</label>
                            <input name="section" value={formData.section} onChange={handleChange} className="input-field" placeholder="section-a" />
                        </div>

                        <div>
                            <label className="label">Subject (Folder Name)</label>
                            <input name="subject" value={formData.subject} onChange={handleChange} className="input-field" placeholder="networks" />
                        </div>
                    </div>

                    <div>
                        <label className="label">Markdown Content</label>
                        <textarea
                            name="content"
                            value={formData.content}
                            onChange={handleChange}
                            className="w-full h-96 p-6 font-mono text-sm border dark:border-gray-600 rounded-2xl bg-gray-50 dark:bg-gray-900 dark:text-gray-300 focus:ring-4 focus:ring-blue-500/20 outline-none transition-all"
                            required
                        />
                    </div>

                    <div className="flex gap-4">
                        <button type="submit" className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-2xl transition-all shadow-xl shadow-blue-200 dark:shadow-none">
                            {isEditing ? '💾 Update Note' : '🚀 Publish Note'}
                        </button>
                        {isEditing && (
                            <button type="button" onClick={() => { setIsEditing(false); setActiveTab('dashboard'); }} className="px-8 bg-gray-100 dark:bg-gray-700 font-bold rounded-2xl">Cancel</button>
                        )}
                    </div>
                </form>
            )}
        </div>
    );
};

// Simple CSS helper for this page
const css = `
.label { display: block; margin-bottom: 0.5rem; font-weight: 500; color: #4b5563; }
.dark .label { color: #d1d5db; }
.input-field { width: 100%; padding: 0.75rem; border-radius: 0.5rem; border: 1px solid #d1d5db; background-color: #fff; }
.dark .input-field { background-color: #374151; border-color: #4b5563; color: #fff; }
`;

export default () => (
    <>
        <style>{css}</style>
        <TeacherUpload />
    </>
);