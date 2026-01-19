import type { Meta, StoryObj } from '@storybook/react';
import { TreeNode } from './FolderTree';
import { FileSystemNode } from './types';
import { MemoryRouter } from 'react-router-dom';

const meta: Meta<typeof TreeNode> = {
    title: 'Organisms/FolderTree',
    component: TreeNode,
    decorators: [
        (Story) => (
            <MemoryRouter>
                <div className="max-w-md mx-auto p-4 bg-gray-50 dark:bg-gray-900 min-h-screen">
                    <Story />
                </div>
            </MemoryRouter>
        ),
    ],
};

export default meta;
type Story = StoryObj<typeof TreeNode>;

const mockLeaf: FileSystemNode = {
    id: 'note-1',
    name: 'Binary Trees',
    type: 'file',
    path: '/cs/y2/ds/trees/1',
    level: 3,
    fileData: {
        id: 1,
        publicId: '123',
        title: 'Binary Trees',
        department: 'CS',
        year: 'year-2',
        subject: 'DS',
        status: 'PUBLISHED',
        currentVersion: 1,
        updatedAt: '2025-01-01T10:00:00',
        uploadedByName: 'Prof X',
        content: ''
    }
};

const mockFolder: FileSystemNode = {
    id: 'subject-ds',
    name: 'Data Structures',
    type: 'folder',
    path: '/cs/y2/ds',
    level: 2,
    children: [mockLeaf, { ...mockLeaf, id: 'note-2', name: 'Graphs' }]
};

const mockRoot: FileSystemNode = {
    id: 'dept-cs',
    name: 'Computer Science',
    type: 'folder',
    path: '/cs',
    level: 0,
    children: [
        {
            id: 'year-2',
            name: 'Year 2',
            type: 'folder',
            path: '/cs/y2',
            level: 1,
            children: [mockFolder]
        }
    ]
};

export const SingleFolder: Story = {
    args: {
        node: mockFolder,
        level: 0
    },
};

export const FullTree: Story = {
    args: {
        node: mockRoot,
        level: 0
    },
};
