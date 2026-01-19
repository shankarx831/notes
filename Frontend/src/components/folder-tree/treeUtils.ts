import { FileSystemNode } from './types';
import { Note } from '@/types';

// Helper to transform the legacy nested object structure into a normalized recursive tree
export function transformToTree(
    rawTree: Record<string, any>,
    rootPath: string = ''
): FileSystemNode[] {
    return Object.keys(rawTree).map((key) => {
        const value = rawTree[key];
        // If it's an array, it's a list of Notes (leaf nodes)
        if (Array.isArray(value)) {
            // We are at the Subject Level. logical path: /dept/year/section/subject
            // rootPath is already "/dept/year/section/subject" (passed from parent)
            // actually, the parent passed `rootPath` which was `/dept/year/section`.
            // The current Loop Key is `subject`.
            // So the full path context for these notes is `rootPath + '/' + key`.

            const contextPath = `${rootPath}/${key}`.replace(/^\//, ''); // e.g. "cs/year1/section-a/networks"
            const [dept, year, section, subject] = contextPath.split('/');

            return {
                id: key, // The Subject Name (e.g. "Networks")
                name: key,
                type: 'folder',
                path: `/${contextPath}`,
                level: 0,
                children: value.map((rawNote: any) => {
                    // Normalize the raw loader object into a proper Note interface
                    // The loader gives: { id, type, meta: { title, ... }, content }
                    const noteTitle = rawNote.title || rawNote.meta?.title || rawNote.id;

                    // Construct a synthetic Note object ensuring all required fields are present
                    const normalizedNote: Note = {
                        id: rawNote.id, // String filename usually, but Note interface says number. We might need to cast or parse. 
                        // Actually Note interface says id: number. Loader uses string filename.
                        // For static mode, let's just use 0 or hash, or force cast/string in Types. 
                        // Ideally we update Note type to id: string | number.
                        publicId: rawNote.id,
                        title: noteTitle,
                        content: rawNote.content || '',
                        department: dept || 'unknown',
                        year: year || 'unknown',
                        section: section || 'unknown',
                        subject: subject || 'unknown',
                        status: 'PUBLISHED',
                        currentVersion: 1,
                        updatedAt: new Date().toISOString(), // Static files don't have date easily unless in meta
                        uploadedByName: 'System',
                        ...rawNote, // fallback for any other props
                    };

                    return {
                        id: rawNote.id,
                        name: noteTitle,
                        type: 'file',
                        path: `/${contextPath}/${rawNote.id}`,
                        fileData: normalizedNote, // Now perfectly compatible with Home.tsx
                        level: 0
                    };
                })
            } as FileSystemNode;
        }

        // Otherwise it's a folder (Year, Dept, Section)
        // We recurse down.
        return {
            id: key,
            name: key,
            type: 'folder',
            path: `${rootPath}/${key}`,
            level: 0,
            children: transformToTree(value, `${rootPath}/${key}`)
        };
    });
}
