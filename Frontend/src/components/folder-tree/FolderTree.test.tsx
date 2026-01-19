import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import '@testing-library/jest-dom';
import { TreeNode } from './FolderTree';
import { FileSystemNode } from './types';
import { BrowserRouter } from 'react-router-dom';

const mockNode: FileSystemNode = {
    id: 'folder-1',
    name: 'Test Folder',
    type: 'folder',
    path: '/test',
    level: 0,
    children: [
        {
            id: 'file-1',
            name: 'Test File',
            type: 'file',
            path: '/test/file',
            level: 1,
            fileData: {
                id: 1,
                publicId: '1',
                title: 'Test File',
                department: 'test',
                year: 'y1',
                subject: 'sub',
                status: 'PUBLISHED',
                currentVersion: 1,
                updatedAt: '2025-01-01',
                uploadedByName: 'Me',
                content: ''
            }
        }
    ]
};

describe('FolderTree', () => {
    it('renders folder initially closed', () => {
        render(<BrowserRouter><TreeNode node={mockNode} level={0} /></BrowserRouter>);
        expect(screen.getByText('Test Folder')).toBeInTheDocument();
        expect(screen.queryByText('Test File')).not.toBeInTheDocument();
    });

    it('expands folder on click', () => {
        render(<BrowserRouter><TreeNode node={mockNode} level={0} /></BrowserRouter>);
        const folder = screen.getByText('Test Folder');
        fireEvent.click(folder);
        // Animation might delay visibility, but in JSDOM usually instant or we wait
        // However, AnimatePresence often keeps it in DOM. 
        // We check if it APPEARS.
        expect(screen.getByText('Test File')).toBeInTheDocument();
    });
});
