using System.Text;

namespace TrickleDeobfuscatorTool
{
  internal static class Program
  {
    static Program()
    {
      Encoding.RegisterProvider(CodePagesEncodingProvider.Instance);
    }

    /*static List<Process> GetChildProcesses(int parentId)
    {
      var query = "Select * From Win32_Process Where ParentProcessId = "
                  + parentId;
      ManagementObjectSearcher searcher = new ManagementObjectSearcher(query);
      ManagementObjectCollection processList = searcher.Get();

      var result = processList.Cast<ManagementObject>()
        .Select(p => Process.GetProcessById(Convert.ToInt32(p.GetPropertyValue("ProcessId"))))
        .ToList();

      return result;
    }*/

    /// <summary>
    ///  The main entry point for the application.
    /// </summary>
    [STAThread]
    static void Main()
    {
      ApplicationConfiguration.Initialize();
      Application.Run(new Form1());
    }
  }
}