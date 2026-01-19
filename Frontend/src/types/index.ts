export type UserRole = 'ROLE_STUDENT' | 'ROLE_TEACHER' | 'ROLE_ADMIN';

export interface User {
    id: number;
    publicId: string;
    email: string;
    name: string;
    role: UserRole;
    assignedDepartments?: string[];
}

export type NoteStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'DELETE_PENDING' | 'DELETED';

export interface Note {
    id: number | string;
    publicId: string;
    title: string;
    content: string; // Markdown
    department: string;
    year: string;
    section?: string;
    subject: string;
    status: NoteStatus;
    currentVersion: number;
    updatedAt: string;
    uploadedByName: string;
}

export interface FolderNode {
    name: string;
    path: string;
    type: 'folder' | 'file';
    children?: FolderNode[];
    data?: Note; // If file
}
