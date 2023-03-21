const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');

const REMOTE_DEBUGGING_PORT = 9222;

function createWindow() {
  const mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
      webSecurity: false,
      devTools: true,
    },
  });

  mainWindow.loadFile(path.join(__dirname, 'index.html'));

  ipcMain.on('submitText', (_, inputText) => {
    const message = inputText || 'No text provided';
    mainWindow.webContents.send('updateDisplayText', message);
  });
}

app.commandLine.appendSwitch('remote-debugging-port', REMOTE_DEBUGGING_PORT);

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});
