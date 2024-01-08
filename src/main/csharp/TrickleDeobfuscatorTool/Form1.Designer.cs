namespace TrickleDeobfuscatorTool
{
  partial class Form1
  {
    /// <summary>
    ///  Required designer variable.
    /// </summary>
    private System.ComponentModel.IContainer components = null;

    /// <summary>
    ///  Clean up any resources being used.
    /// </summary>
    /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
    protected override void Dispose(bool disposing)
    {
      if (disposing && (components != null))
      {
        components.Dispose();
      }
      base.Dispose(disposing);
    }

    #region Windows Form Designer generated code

    /// <summary>
    ///  Required method for Designer support - do not modify
    ///  the contents of this method with the code editor.
    /// </summary>
    private void InitializeComponent()
    {
      this._selectExeBtn = new System.Windows.Forms.Button();
      this._selectedTrickleExelbl = new System.Windows.Forms.Label();
      this._selectResultsBtn = new System.Windows.Forms.Button();
      this._deobfuscationResultsStoreDirLbl = new System.Windows.Forms.Label();
      this._startBtn = new System.Windows.Forms.Button();
      this._progressBar = new System.Windows.Forms.ProgressBar();
      this.SuspendLayout();
      // 
      // _selectExeBtn
      // 
      this._selectExeBtn.Location = new System.Drawing.Point(12, 12);
      this._selectExeBtn.Name = "_selectExeBtn";
      this._selectExeBtn.Size = new System.Drawing.Size(846, 64);
      this._selectExeBtn.TabIndex = 0;
      this._selectExeBtn.Text = "Select Trickle.exe";
      this._selectExeBtn.UseVisualStyleBackColor = true;
      this._selectExeBtn.Click += new System.EventHandler(this.button1_Click);
      // 
      // _selectedTrickleExelbl
      // 
      this._selectedTrickleExelbl.AutoSize = true;
      this._selectedTrickleExelbl.Location = new System.Drawing.Point(14, 82);
      this._selectedTrickleExelbl.Name = "_selectedTrickleExelbl";
      this._selectedTrickleExelbl.Size = new System.Drawing.Size(320, 37);
      this._selectedTrickleExelbl.TabIndex = 2;
      this._selectedTrickleExelbl.Text = "<No Trickle.exe selected>";
      // 
      // _selectResultsBtn
      // 
      this._selectResultsBtn.Location = new System.Drawing.Point(12, 175);
      this._selectResultsBtn.Name = "_selectResultsBtn";
      this._selectResultsBtn.Size = new System.Drawing.Size(846, 70);
      this._selectResultsBtn.TabIndex = 3;
      this._selectResultsBtn.Text = "Select deobfuscation results directory";
      this._selectResultsBtn.UseVisualStyleBackColor = true;
      this._selectResultsBtn.Click += new System.EventHandler(this.button2_Click);
      // 
      // _deobfuscationResultsStoreDirLbl
      // 
      this._deobfuscationResultsStoreDirLbl.AutoSize = true;
      this._deobfuscationResultsStoreDirLbl.Location = new System.Drawing.Point(14, 266);
      this._deobfuscationResultsStoreDirLbl.Name = "_deobfuscationResultsStoreDirLbl";
      this._deobfuscationResultsStoreDirLbl.Size = new System.Drawing.Size(309, 37);
      this._deobfuscationResultsStoreDirLbl.TabIndex = 4;
      this._deobfuscationResultsStoreDirLbl.Text = "<No deobf dir selected>";
      // 
      // _startBtn
      // 
      this._startBtn.Enabled = false;
      this._startBtn.Location = new System.Drawing.Point(14, 426);
      this._startBtn.Name = "_startBtn";
      this._startBtn.Size = new System.Drawing.Size(844, 119);
      this._startBtn.TabIndex = 5;
      this._startBtn.Text = "Start";
      this._startBtn.UseVisualStyleBackColor = true;
      this._startBtn.Click += new System.EventHandler(this._startBtn_Click);
      // 
      // _progressBar
      // 
      this._progressBar.Location = new System.Drawing.Point(14, 372);
      this._progressBar.Name = "_progressBar";
      this._progressBar.Size = new System.Drawing.Size(844, 48);
      this._progressBar.Style = System.Windows.Forms.ProgressBarStyle.Marquee;
      this._progressBar.TabIndex = 6;
      this._progressBar.Visible = false;
      // 
      // Form1
      // 
      this.AutoScaleDimensions = new System.Drawing.SizeF(15F, 37F);
      this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
      this.ClientSize = new System.Drawing.Size(870, 557);
      this.Controls.Add(this._progressBar);
      this.Controls.Add(this._startBtn);
      this.Controls.Add(this._deobfuscationResultsStoreDirLbl);
      this.Controls.Add(this._selectResultsBtn);
      this.Controls.Add(this._selectedTrickleExelbl);
      this.Controls.Add(this._selectExeBtn);
      this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
      this.Name = "Form1";
      this.ShowIcon = false;
      this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
      this.Text = "Trickle decompiler";
      this.ResumeLayout(false);
      this.PerformLayout();

    }

    #endregion

    private Button _selectExeBtn;
    private Label _selectedTrickleExelbl;
    private Button _selectResultsBtn;
    private Label _deobfuscationResultsStoreDirLbl;
    private Button _startBtn;
    private ProgressBar _progressBar;
  }
}