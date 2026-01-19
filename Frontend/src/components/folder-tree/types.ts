import { Note } from '../types';

export interface FileSystemNode {
    id: string;
    name: string;
    type: 'folder' | 'file';
    path: string; // Breadcrumb path or recursive ID
    children?: FileSystemNode[];
    fileData?: Note; // Only for files
    level: number;
}

export interface FolderTreeProps {
    data: Record<string, any>; // The raw tree from App.jsx
    onSelectNote?: (note: Note) => void;
}
