namespace TrickleDeobfuscatorTool;

public interface IMemoryDumpCreator
{
  Task<bool> CreateAsync(int processId, string outputFileName, CancellationToken ct = default);
}