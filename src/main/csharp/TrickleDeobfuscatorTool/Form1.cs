using PeNet;
using System.Diagnostics;
using System.IO.Compression;
using System.Text;
using ZipFile = Ionic.Zip.ZipFile;

namespace TrickleDeobfuscatorTool
{
  public partial class Form1 : Form
  {
    private const string DeobfuscatorJarName = "deobf.jar";

    public Form1()
    {
      InitializeComponent();
    }

    private async void button1_Click(object sender, EventArgs e)
    {
      var openFileDialog = new OpenFileDialog
      {
        Filter = "Trickle Executable|Trickle.exe|All Executables|*.exe|All Files|*.*",
        InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory),
      };

      if (openFileDialog.ShowDialog() != DialogResult.OK)
      {
        return;
      }

      _selectedTrickleExelbl.Text = openFileDialog.FileName;
      TryEnableStartBtn();
    }

    private void button2_Click(object sender, EventArgs e)
    {
      var openFileDialog = new FolderBrowserDialog
      {
        InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory),
      };

      if (openFileDialog.ShowDialog() != DialogResult.OK)
      {
        return;
      }

      _deobfuscationResultsStoreDirLbl.Text = openFileDialog.SelectedPath;
      TryEnableStartBtn();
    }

    private void TryEnableStartBtn()
    {
      _startBtn.Enabled = !string.IsNullOrEmpty(_selectedTrickleExelbl.Text)
                          && !string.IsNullOrEmpty(_deobfuscationResultsStoreDirLbl.Text);
    }

    private async void _startBtn_Click(object sender, EventArgs e)
    {
      _selectExeBtn.Enabled = false;
      _selectResultsBtn.Enabled = false;
      _startBtn.Enabled = false;
      _progressBar.Visible = true;
      try
      {
        MessageBox.Show("Do not run any java based applications during deobfuscation process", "Important!!",
          MessageBoxButtons.OK, MessageBoxIcon.Warning);
        await Task.Run(() => Deobfuscate(_selectedTrickleExelbl.Text, _deobfuscationResultsStoreDirLbl.Text));
        MessageBox.Show("Deobfuscated successfully", "Done");
      }
      catch (Exception exc)
      {
        MessageBox.Show(exc.Message, "Failed to deobfuscate");
      }
      finally
      {
        _progressBar.Visible = false;
        _selectedTrickleExelbl.Text = "<No Trickle.exe selected>";
        _deobfuscationResultsStoreDirLbl.Text = "<No deobf dir selected>";
        _selectExeBtn.Enabled = true;
        _selectResultsBtn.Enabled = true;
        TryEnableStartBtn();
      }
    }

    private async Task Deobfuscate(string trickleExe, string deobfuscationResultsStoreDir)
    {
      var procInfo = new ProcessStartInfo
      {
        UseShellExecute = false,
        FileName = trickleExe,
        CreateNoWindow = true,
      };

      var process = Process.Start(procInfo)
                    ?? throw new InvalidOperationException("Failed to start Trickle application");

      var runningJavawIds = Process.GetProcessesByName("javaw")
        .Select(_ => _.Id)
        .ToHashSet();

      if (runningJavawIds.Count > 0)
      {
        Invoke(() =>
        {
          MessageBox.Show("Please close all javaw and Trickle processes. PIDs: " + string.Join(", ", runningJavawIds),
            "Failure",
            MessageBoxButtons.OK, MessageBoxIcon.Error);
        });
        return;
      }

      int? javawProcessId = null;
      do
      {
        var javawProcess = Process.GetProcessesByName("javaw")
          .ExceptBy(runningJavawIds, _ => _.Id)
          .FirstOrDefault();
        if (javawProcess is not null)
        {
          javawProcessId = javawProcess.Id;
        }
        else
        {
          Thread.Sleep(TimeSpan.FromMilliseconds(500));
        }
      } while (!javawProcessId.HasValue);

      var workingDir = Path.Combine(Path.GetTempPath(), "trickle.deobf." + DateTime.Now.ToString("yyyyMMddHHmmss"));
      if (!Directory.Exists(workingDir))
      {
        Directory.CreateDirectory(workingDir);
      }

      var memDumpPath = Path.Combine(workingDir, "trickle.mem.dmp");
      var extractedJarPath = Path.Combine(workingDir, "ExtractedJar");
      var dumpCreator = new ProcdumpBasedMemoryDumpCreator();
      var attemptCount = 0;
      const int maxAttempts = 10;

      bool memDumpCreated;
      bool extracted = false;
      do
      {
        memDumpCreated = await dumpCreator.CreateAsync(javawProcessId.Value, memDumpPath);
        if (!memDumpCreated)
        {
          attemptCount++;
          await Task.Delay(TimeSpan.FromMilliseconds(500));
          continue;
        }


        byte[] rdataBytes = await File.ReadAllBytesAsync(memDumpPath);
        var startHeader = new byte[] { 0x50, 0x4b, 0x03, 0x04 };
        int size = 0;
        for (var ix = 0; ix < rdataBytes.Length; ix++)
        {
          var b = rdataBytes[ix];
          if (b == startHeader[0] && rdataBytes[ix..(ix + startHeader.Length)].SequenceEqual(startHeader))
          {
            var possiblyZipArchive = rdataBytes[ix..];
            try
            {
              var sizeMb = possiblyZipArchive.Length / 1024 / 1024;
              if (size != sizeMb)
              {
                size = sizeMb;
                Console.WriteLine(sizeMb);
              }

              var zipFile = ZipFile.Read(new MemoryStream(possiblyZipArchive));
              if (zipFile.Entries.Count == 0)
              {
                continue;
              }

              zipFile.ExtractAll(extractedJarPath);
              extracted = true;
              break;
            }
            catch (Exception)
            {
              // ignore
            }
          }
        }
      } while (!memDumpCreated || !extracted || attemptCount >= maxAttempts);

      if (!memDumpCreated)
      {
        Invoke(() =>
        {
          MessageBox.Show("Failed to make memory dump!", "Failure", MessageBoxButtons.OK, MessageBoxIcon.Error);
        });
        return;
      }

      process!.Kill(true);
      await process.WaitForExitAsync();


      var jarStartInfo = new ProcessStartInfo
      {
        FileName = "java",
        Arguments = $"-jar \"{DeobfuscatorJarName}\" \"{extractedJarPath}\" \"{deobfuscationResultsStoreDir}\"",
        UseShellExecute = false,
      };

      await Process.Start(jarStartInfo)!.WaitForExitAsync();
      Process.Start("explorer.exe", deobfuscationResultsStoreDir);
      try
      {
        Directory.Delete(workingDir, true);
      }
      catch
      {
      }
    }
  }
}