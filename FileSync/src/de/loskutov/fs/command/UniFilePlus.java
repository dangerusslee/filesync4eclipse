package de.loskutov.fs.command;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * This is a copy of @link org.eclipse.rse.internal.importexport.files.UniFilePlus of
 * org.eclipse.rse.importexport.source_1.2.1.v200909160005.jar which was available within the
 * RSE-Plugin at http://www.eclipse.org/dsdp/tm/ . As older versions of this class were different
 * and maybe future versions as well, I created my own copy and requested to remove this class from
 * the restricted area.
 * <p>
 * see  //dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.rse/plugins/org.eclipse.rse.importexport/src/org/eclipse/rse/internal/importexport/files/?root=DSDP_Project}
 */
public class UniFilePlus extends File {

    /**
     *
     */
    private static final long serialVersionUID = -1717648997950319457L;
    public IRemoteFile remoteFile = null;

    /**
     * Constructor. There is only one way to construct this object, and that is by giving an
     * IRemoteFile object. All java.io.File methods are intercepted and delegated to this contained
     * object.
     */
    public UniFilePlus(IRemoteFile remoteFile) {
        super(remoteFile.getAbsolutePath());
        this.remoteFile = remoteFile;
    }

    public boolean canRead() {
        return remoteFile.canRead();
    }

    public boolean canWrite() {
        return remoteFile.canWrite();
    }

    public int compareTo(File pathname) {
        if (pathname instanceof UniFilePlus) {
            return remoteFile.compareTo(pathname);
        }
        return super.compareTo(pathname);
    }

    /*
     * public int compareTo(Object o) { return remoteFile.compareTo(o); }
     */
    public boolean createNewFile() throws IOException {
        IRemoteFile newFile = null;
        try {
            newFile = remoteFile.getParentRemoteFileSubSystem().createFile(remoteFile,
                    new NullProgressMonitor());
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if (e != null) {
                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                } else if (e instanceof IOException) {
                    throw (IOException) e;
                }
            }
            if (exc instanceof RemoteFileSecurityException) {
                throw new SecurityException(exc.getMessage());
            }
            throw new IOException(exc.getMessage());
        } catch (SystemMessageException e) {
            throw new IOException(e.getMessage());
        }
        if (newFile != null) {
            remoteFile = newFile;
            return true;
        }
        return false;
    }

    public boolean delete() {
        boolean ok = true;
        try {
            remoteFile.getParentRemoteFileSubSystem().delete(remoteFile, new NullProgressMonitor());
            // hmm, should we set remoteFile to null?
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
            ok = false;
        }
        return ok;
    }

    /**
     * NOT SUPPORTED!
     */
    public void deleteOnExit() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        return remoteFile.equals(obj);
    }

    public boolean exists() {
        return remoteFile.exists();
    }

    public File getAbsoluteFile() {
        return this; // Remote File objects are always absolute!
    }

    public String getAbsolutePath() {
        return remoteFile.getAbsolutePath();
    }

    public File getCanonicalFile() {
        // hmm, maybe we should equal getAbsolutePathPlusConnection as
        // canonical!
        return this;
    }

    public String getCanonicalPath() {
        return remoteFile.getAbsolutePathPlusConnection();
    }

    public String getName() {
        return remoteFile.getName();
    }

    public String getParent() {
        return remoteFile.getParentPath();
    }

    public File getParentFile() {
        IRemoteFile parentFolder = remoteFile.getParentRemoteFileSubSystem().getParentFolder(
                remoteFile, null);

        if (parentFolder != null) {
            return new UniFilePlus(parentFolder);
        }
        return null;
    }

    public String getPath() {
        return remoteFile.getAbsolutePath();
    }

    public int hashCode() {
        return remoteFile.getAbsolutePathPlusConnection().hashCode();
    }

    public boolean isAbsolute() {
        return true;
    }

    public boolean isDirectory() {
        return remoteFile.isDirectory();
    }

    public boolean isFile() {
        return remoteFile.isFile();
    }

    public boolean isHidden() {
        return remoteFile.isHidden();
    }

    public long lastModified() {
        return remoteFile.getLastModified();
    }

    public long length() {
        return remoteFile.getLength();
    }

    /**
     * Returns an array of remote files that are children of this folder. This will be an null if
     * there is an error or if the target object is not a folder.
     *
     * @return the array of IRemoteFiles.
     */
    public IRemoteFile[] listIRemoteFiles() {
        IRemoteFile[] result = null;
        try {
            result = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, getNullMonitor());
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        return result;
    }

    public String[] list() {
        IRemoteFile[] files = null;
        try {
            files = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, getNullMonitor());
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        if (files != null) {
            String[] fileNames = new String[files.length];
            for (int idx = 0; idx < files.length; idx++) {
                fileNames[idx] = files[idx].getName();
            }
            return fileNames;
        }
        return null;
    }

    public File[] listFiles() {
        IRemoteFile[] files = null;

        try {
            files = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, null);
        } catch (SystemMessageException e) {
            e.printStackTrace();
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        if (files != null) {
            Vector<UniFilePlus> children = new Vector<UniFilePlus> ();
            for (int i = 0; i < files.length; i++) {
                // fileName = files[idx].getName();
                UniFilePlus fileObj = new UniFilePlus(files[i]);
                children.addElement(fileObj);
            }
            UniFilePlus[] fileObjs = new UniFilePlus[children.size()];
            // for (int i = 0; i < children.size(); i++)
            // fileObjs[i] = (UniFilePlus) children.elementAt(i);
            // return fileObjs;
            return children.toArray(fileObjs);
        }
        return null;
    }

    public String[] list(FilenameFilter filter) {
        IRemoteFile[] files = null;
        try {
            files = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, getNullMonitor());
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        if (files != null) {
            Vector v = new Vector();
            String fileName = null;
            for (int idx = 0; idx < files.length; idx++) {
                fileName = files[idx].getName();
                if ((fileName != null) && (filter.accept(this, fileName))) {
                    v.addElement(fileName);
                }
            }
            String[] fileNames = new String[v.size()];
            for (int idx = 0; idx < v.size(); idx++) {
                fileNames[idx] = (String) v.elementAt(idx);
            }
            return fileNames;
        }
        return null;
    }

    public File[] listFiles(FileFilter filter) {
        IRemoteFile[] files = null;
        try {
            files = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, getNullMonitor());
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        if (files != null) {
            Vector v = new Vector();
            for (int idx = 0; idx < files.length; idx++) {
                // fileName = files[idx].getName();
                File fileObj = new File(files[idx].getAbsolutePath());
                if (filter.accept(fileObj)) {
                    v.addElement(fileObj);
                }
            }
            File[] fileObjs = new File[v.size()];
            for (int idx = 0; idx < v.size(); idx++) {
                fileObjs[idx] = (File) v.elementAt(idx);
            }
            return fileObjs;
        }
        return null;
    }

    public File[] listFiles(FilenameFilter filter) {
        IRemoteFile[] files = null;
        try {
            files = remoteFile.getParentRemoteFileSubSystem().list(remoteFile, getNullMonitor());
        } catch (SystemMessageException e) {
            SystemBasePlugin.logError("unexpected exception", e); //$NON-NLS-1$
        }
        if (files != null) {
            Vector v = new Vector();
            String fileName = null;
            for (int idx = 0; idx < files.length; idx++) {
                fileName = files[idx].getName();
                if ((fileName != null) && (filter.accept(this, fileName))) {
                    v.addElement(files[idx]);
                }
            }
            File[] fileObjs = new File[v.size()];
            for (int idx = 0; idx < v.size(); idx++) {
                fileObjs[idx] = new File(((IRemoteFile) v.elementAt(idx)).getAbsolutePath());
            }
            return fileObjs;
        }
        return null;
    }

    public boolean mkdir() {
        IRemoteFile dir = null;
        try {
            if (!remoteFile.exists()) {
                dir = remoteFile.getParentRemoteFileSubSystem().createFolder(remoteFile,
                        new NullProgressMonitor());
            }
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
        }
        if (dir != null) {
            remoteFile = dir;
        }
        return (dir != null);
    }

    public boolean mkdirs() {
        IRemoteFile dir = null;
        try {
            if (!remoteFile.exists()) {
                dir = remoteFile.getParentRemoteFileSubSystem().createFolders(remoteFile,
                        new NullProgressMonitor());
            }
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
        }
        if (dir != null) {
            remoteFile = dir;
        }
        return (dir != null);
    }

    public boolean renameTo(File dest) {
        boolean ok = false;
        try {
            remoteFile.getParentRemoteFileSubSystem().rename(remoteFile, dest.getName(),
                    new NullProgressMonitor());
            ok = true;
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
        }
        return ok;
    }

    public boolean setLastModified(long time) {
        boolean ok = false;
        if (time < 0) {
            throw new IllegalArgumentException();
        }
        try {
            IProgressMonitor monitor = new NullProgressMonitor();
            remoteFile.getParentRemoteFileSubSystem().setLastModified(remoteFile, time, monitor);
            ok = true;
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
        }
        return ok;
    }

    public boolean setReadOnly() {
        boolean ok = false;
        try {
            remoteFile.getParentRemoteFileSubSystem().setReadOnly(remoteFile, true,
                    new NullProgressMonitor());
            ok = true;
        } catch (RemoteFileException exc) {
            Exception e = exc.getRemoteException();
            if ((e != null) && (e instanceof SecurityException)) {
                throw (SecurityException) e;
            }
            throw new SecurityException(exc.getMessage());
        } catch (SystemMessageException e) {
            // TODO should there be more user feedback?
            SystemBasePlugin.logMessage(e.getSystemMessage());
        }
        return ok;
    }

    public String toString() {
        return getPath();
    }

    public URL toURL() throws MalformedURLException {
        String urlName = "file://" + remoteFile.getAbsolutePathPlusConnection().replace('\\', '/'); //$NON-NLS-1$
        if (remoteFile.isDirectory() && !urlName.endsWith("/")) {
            urlName = urlName + '/';
        }
        return new URL(urlName);
    }

    private IProgressMonitor getNullMonitor() {
        IProgressMonitor result = new NullProgressMonitor();
        return result;
    }

    public IRemoteFile getRemoteFile() {
        return remoteFile;
    }

    public InputStream getInputStream() throws SystemMessageException {
        return remoteFile.getParentRemoteFileSubSystem().getInputStream(
                remoteFile.getParentPath(), remoteFile.getName(),
                remoteFile.isBinary(), null);
    }

    public void synchRemoteFile() {
        // get the latest version of the remote file
        remoteFile.markStale(true);
        try {
            remoteFile = remoteFile.getParentRemoteFileSubSystem().getRemoteFileObject(
                    remoteFile.getAbsolutePath(), new NullProgressMonitor());
        } catch (Exception e) {/**/
        }
    }
}
