using System.Diagnostics;

namespace TrickleDeobfuscatorTool;

public class ProcdumpBasedMemoryDumpCreator : IMemoryDumpCreator
{
  private static readonly string ProcdumpRelativePath = Path.Combine("tools", "procdump64.exe");
  private const string Switches = "-accepteula -o -ma ";

  public async Task<bool> CreateAsync(int processId, string outputFileName, CancellationToken ct = default)
  {
    try
    {
      var arguments = $"{Switches} {processId.ToString()} \"{outputFileName}\"";
      var dumpStartInfo = new ProcessStartInfo(ProcdumpRelativePath, arguments)
      {
        RedirectStandardOutput = true,
        CreateNoWindow = true
      };

      var memoryDumpProcess = new Process
      {
        StartInfo = dumpStartInfo
      };

      memoryDumpProcess.Start();
      while (!memoryDumpProcess.StandardOutput.EndOfStream)
      {
        var output = await memoryDumpProcess.StandardOutput.ReadToEndAsync();
        if (output.Contains("Error writing dump file", StringComparison.InvariantCultureIgnoreCase))
        {
          await memoryDumpProcess.WaitForExitAsync(ct);
          return false;
        }
        LogDebug(output);
        memoryDumpProcess.StandardOutput.DiscardBufferedData();
      }

      await memoryDumpProcess.WaitForExitAsync(ct);
      return true;
    }
    catch (Exception e)
    {
      LogError(e, "Can't create memory dump");
      return false;
    }
  }

  private static void LogDebug(string message) => Debug.WriteLine(message);
  private static void LogError(Exception exc, string message) => Debug.WriteLine(message);
}